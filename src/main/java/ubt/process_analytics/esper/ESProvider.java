package ubt.process_analytics.esper;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.internal.event.bean.core.BeanEventBean;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.CompilerOptions;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.compiler.client.option.StatementNameContext;
import com.espertech.esper.compiler.client.option.StatementNameOption;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.EPDeploymentService;
import javax.swing.JFrame;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import com.opencsv.exceptions.CsvValidationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import ubt.process_analytics.utils.*;

public class ESProvider {

    private final EPRuntime epRuntime;
    private final EPDeploymentService deploymentService;
    private final EPStreamMetrics metrics;
    private final PROBS config = PROBS.getInstance();
    private final boolean ENABLE_HEURISTICS_MINER = true;
    private final LossyCountingHeuristicsMiner heuristicsMiner = new LossyCountingHeuristicsMiner(0.01);
    private final PerformanceEvaluator performanceEvaluator = new PerformanceEvaluator();

    public ESProvider(List<ESTemplate> templates) {
        Configuration configuration = configureEsper();

        this.epRuntime = EPRuntimeProvider.getDefaultRuntime(configuration);
        this.deploymentService = epRuntime.getDeploymentService();
        this.metrics = new EPStreamMetrics(this.heuristicsMiner, false);

        batchTemplateAdding(templates, configuration);
    }

    private void batchTemplateAdding(List<ESTemplate> templates, Configuration configuration) {
        for (ESTemplate template : templates) {
            String epl = template.getEplQuery();
            EPStatement[] statements = this.compileAndDeploy(epl, configuration, template.getTemplateName());
            if (statements != null) {
                System.out.println(STR."Added template \{template.getTemplateName()}");

                for (EPStatement statement : statements) {
                    this.addListener(Objects.requireNonNull(statement));
                }
            } else {
                System.out.println(STR."ERROR Adding the following template:\{template.getTemplateName()}");
            }

        }
    }

    private Configuration configureEsper() {
        Configuration configuration = new Configuration();
        configuration.getCommon().addEventType(EPPMEventType.class.getName(), EPPMEventType.class);
        return configuration;
    }

    private EPStatement[] compileAndDeploy(String epl, Configuration configuration, String templateName) {
        EPCompiler compiler = EPCompilerProvider.getCompiler();

        CompilerArguments args = new CompilerArguments(configuration);
        CompilerOptions ops = new CompilerOptions();
        args.setOptions(ops.setStatementName(_ -> templateName));

        try {
            EPCompiled epCompiled = compiler.compile(epl, args);
            EPDeployment deployment = deploymentService.deploy(epCompiled);
            return deployment.getStatements();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addListener(EPStatement statement) {
        statement.addListener((newData, oldData, stat, rt) -> {
            if (newData != null) {
                for (EventBean eventBean : newData) {
                    EPPMEventType match = (EPPMEventType) eventBean.getUnderlying();

                    System.out.println(STR."[\{stat.getName()}] Query matched! Event details: \{match}");

                    // Increment the metrics counter
                    this.metrics.incrementEventCount();
                }
            }
        });
    }

    public void sendEvents(EPPMEventType event) {
        // Record start time for latency calculation
        performanceEvaluator.recordStartTime();

        epRuntime.getEventService().sendEventBean(event, EPPMEventType.class.getName());


        try {
            Thread.sleep(this.config.getInt("ESPER_CONFIG_EVENT_LOOP_SLEEPING_TIME_IN_MS"));

            if (this.ENABLE_HEURISTICS_MINER) {
                this.heuristicsMiner.addEvent(event);
            }
            if (this.ENABLE_HEURISTICS_MINER && this.metrics.getTotalEvents() > 822) {
                List<ESTemplate> convertedTemplate = this.heuristicsMiner.convertToTemplates();
            }

            // Increment the metrics counter
            this.metrics.incrementEventCount();

            // Record event processed for latency calculation
            performanceEvaluator.recordEventProcessed();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the performance data and generates plots using JFreeChart.
     */
    public void getPerformanceData() {
        Map<String, List<?>> performanceData = new HashMap<>();

        List<Double> throughputValues = performanceEvaluator.getThroughputValues();
        List<Long> timestamps = performanceEvaluator.getTimestamps();

        printStatistics(throughputValues);

//        // Create the TimeSeries for latency
//        TimeSeries latencySeries = new TimeSeries("Latency (ms)");
//        for (int i = 0; i < timestamps.size(); i++) {
//            latencySeries.addOrUpdate(new Millisecond(new Date(timestamps.get(i))), latencyValues.get(i) / 1_000_000.0); // Convert nanoseconds to milliseconds
//        }

        // Create the TimeSeries for throughput
        TimeSeries throughputSeries = new TimeSeries("Throughput (events/sec)");
        for (int i = 0; i < timestamps.size(); i++) {
            throughputSeries.addOrUpdate(new Millisecond(new Date(timestamps.get(i))), throughputValues.get(i));
        }

        // Create the dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();

        dataset.addSeries(throughputSeries);

        // Create the chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Performance Metrics for 1,000,000 Events without query",
                "Time",                     // X-axis label
                "Throughput (events/sec)",  // Y-axis label
                dataset,                    // Dataset
                true,                       // Include legend
                false,                       // Tooltips
                false                       // URLs
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setOrientation(PlotOrientation.VERTICAL);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        // Save the chart as an image
        try {

            OutputStream out = new FileOutputStream("test.png");
            ChartUtils.writeChartAsPNG(out,
                    chart,
                    800,
                    600
            );

        } catch (IOException ex) {
            System.out.println("sfsdg");
        }


        // Display the chart


        // Create and set up the JFrame
        JFrame frame = new JFrame("Performance Metrics for 1000000 Events without active queries");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(chartPanel);
        frame.pack();  // Ensures that the components are laid out correctly
        frame.setLocationRelativeTo(null);  // Centers the frame on the screen
        frame.setVisible(true);  // Make sure the frame is visible
    }


    /**
     * Calculates and prints the minimum, maximum, and average values
     * from a list of throughput values.
     *
     * @param throughputValues A list of Double values representing throughput.
     */
    public static void printStatistics(List<Double> throughputValues) {
        if (throughputValues == null || throughputValues.isEmpty()) {
            System.out.println("The list is empty. No statistics to calculate.");
            return;
        }

        double minimum = throughputValues.stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(Double.NaN);

        double maximum = throughputValues.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(Double.NaN);

        double average = throughputValues.stream()
                .mapToDouble(Double::doubleValue)
                .filter(value -> value > 1)  // Consider only values greater than 1
                .average()
                .orElse(Double.NaN);

        System.out.println("Minimum: " + minimum);
        System.out.println("Maximum: " + maximum);
        System.out.println("Average (considering only values > 1): " + average);
    }

}
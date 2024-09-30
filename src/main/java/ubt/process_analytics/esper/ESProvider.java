package ubt.process_analytics.esper;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.internal.event.bean.core.BeanEventBean;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.CompilerOptions;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.EPDeploymentService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;

import ubt.process_analytics.utils.*;

public class ESProvider {

    private final EPRuntime epRuntime;
    private final EPDeploymentService deploymentService;
    private final EPStreamMetrics metrics;
    private final PROBS config = PROBS.getInstance();


    private boolean enableHeuristicsMiner = true;

    public void setLogComplexQueries(boolean logComplexQueries) {
        this.logComplexQueries = logComplexQueries;
    }

    private boolean logComplexQueries = true;


    public void setEnableHeuristicsMiner(boolean enableHeuristicsMiner) {
        this.enableHeuristicsMiner = enableHeuristicsMiner;
    }

    private final LossyCountingHeuristicsMiner heuristicsMiner = new LossyCountingHeuristicsMiner(0.01);

    private Instant startTime;
    private final List<Long> latencyValues = new ArrayList<>();

    public ESProvider(List<ESTemplate> templates ) {
        Configuration configuration = configureEsper();

        this.epRuntime = EPRuntimeProvider.getDefaultRuntime(configuration);
        this.deploymentService = epRuntime.getDeploymentService();
        this.metrics = new EPStreamMetrics(this.heuristicsMiner, this.enableHeuristicsMiner);

        this.batchTemplateAdding(templates, configuration);
        EPStatement[] statements =this.compileAndDeploy(this.createPokemonStatement(), configuration, "SIMPLE_POKEMON_STATEMENT");
        handleStatements("SIMPLE_POKEMON_STATEMENT", statements);
    }

    private void batchTemplateAdding(List<ESTemplate> templates, Configuration configuration) {
        for (ESTemplate template : templates) {
            String epl = template.getEplQuery();
            EPStatement[] statements = this.compileAndDeploy(epl, configuration, STR."\{template.getTemplateName()}");
            handleStatements(template.getTemplateName(), statements);
        }
    }

    private void handleStatements(String templateName, EPStatement[] statements) {
        if (statements != null) {
            System.out.println(STR."Added template \{templateName}");

            for (EPStatement statement : statements) {
                this.addListener(Objects.requireNonNull(statement));
            }
        } else {
            System.out.println(STR."ERROR Adding the following template:\{templateName}");
        }
    }

    private String createPokemonStatement() {
        return STR."SELECT * FROM \{EPPMEventType.class.getName()}";
    }

    private Configuration configureEsper() {
        Configuration configuration = new Configuration();
        configuration.getCommon().addEventType(EPPMEventType.class.getName(), EPPMEventType.class);

        return configuration;
    }

    public long getProcessEventCount() {
        return this.metrics.getTotalEvents();
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

            if (newData == null) {
                return;
            }
            if (Objects.equals(stat.getName(), "SIMPLE_POKEMON_STATEMENT")) {
                // Increment the metrics counter
                this.metrics.incrementEventCount();
                this.handleSimpleEvents(newData);
                return;
            }

            for (EventBean eventBean : newData) {
                List<EPPMEventType> matches = new ArrayList<EPPMEventType>();

                if (newData[0].getUnderlying() instanceof Map) {
                    Map<String, BeanEventBean> eventMap = (Map<String, BeanEventBean>) newData[0].getUnderlying();
                    for (Map.Entry<String, BeanEventBean> entry : eventMap.entrySet()) {
                        String key = entry.getKey();
                        BeanEventBean value = entry.getValue();
                        matches.add((EPPMEventType) value.getUnderlying());
                    }
                } else if (newData[0].getUnderlying() instanceof EPPMEventType) {
                    matches.add((EPPMEventType) eventBean.getUnderlying());
                }

                if(this.logComplexQueries) {
                    System.out.println(STR."[\{stat.getName()}] Query matched! Event details: \{matches}");
                }
            }

        });
    }

    private void handleSimpleEvents(EventBean[] newData) {
        Instant endTime = Instant.now();
        // Calculate the latency
        Duration latency = Duration.between(startTime, endTime);
        this.latencyValues.add(latency.toNanos());
    }

    public void sendEvents(EPPMEventType event) {
        // Record start time for latency calculation
        startTime = Instant.now();

        epRuntime.getEventService().sendEventBean(event, EPPMEventType.class.getName());

        try {
            Thread.sleep(this.config.getInt("ESPER_CONFIG_EVENT_LOOP_SLEEPING_TIME_IN_MS"));

            if (this.enableHeuristicsMiner) {
                this.heuristicsMiner.addEvent(event);
            }
            if (this.enableHeuristicsMiner && this.getProcessEventCount() > 822) {
                List<ESTemplate> convertedTemplate = this.heuristicsMiner.convertToTemplates();
                System.out.println(convertedTemplate);
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the list of latencies.
     *
     * @return A list of latency values in milliseconds.
     */
    public List<Long> getLatencyValues() {
        return latencyValues;
    }


}
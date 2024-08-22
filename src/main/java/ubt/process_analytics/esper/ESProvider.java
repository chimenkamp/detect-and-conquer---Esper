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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.opencsv.exceptions.CsvValidationException;
import ubt.process_analytics.utils.CSVProvider;
import ubt.process_analytics.utils.EPatternRepository;
import ubt.process_analytics.utils.PROBS;

public class ESProvider {

    private final EPRuntime epRuntime;
    private final EPDeploymentService deploymentService;
    private final EPStreamMetrics metrics;
    private final PROBS config = PROBS.getInstance();

    public ESProvider(List<ESTemplate> templates) {
        Configuration configuration = configureEsper();

        this.epRuntime = EPRuntimeProvider.getDefaultRuntime(configuration);
        this.deploymentService = epRuntime.getDeploymentService();
        this.metrics = new EPStreamMetrics();

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

    /**
     * Configures the Esper runtime with necessary settings.
     *
     * @return A configured instance of Configuration.
     */
    private Configuration configureEsper() {
        Configuration configuration = new Configuration();
        configuration.getCommon().addEventType(EPPMEventType.class.getName(), EPPMEventType.class);
        return configuration;
    }

    /**
     * Compiles and deploys an EPL statement.
     *
     * @param epl The EPL statement to compile and deploy.
     * @param configuration The configuration used for compiling the EPL.
     * @return The deployed EPStatement.
     */
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

    /**-
     * Adds a listener to an EPStatement to handle new data events
     *
     * @param statement The EPStatement to add a listener to
     */
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

    /**
     * Continuously sends events to the Esper runtime
     */
    public void sendEvents(EPPMEventType event) {

        epRuntime.getEventService().sendEventBean(event, EPPMEventType.class.getName());
        System.out.println(event);
        try {
            Thread.sleep(this.config.getInt("ESPER_CONFIG_EVENT_LOOP_SLEEPING_TIME_IN_MS"));
            this.metrics.incrementEventCount();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
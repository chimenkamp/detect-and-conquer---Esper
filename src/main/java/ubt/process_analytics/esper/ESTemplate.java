package ubt.process_analytics.esper;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import ubt.process_analytics.utils.EPatternRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ESTemplate {

    private final String est;
    private final String templateName;

    /**
     * Constructs an ESTemplate with the specified parameters and an empty subTemplate array.
     *
     * @param templatePath  The path of the template.
     * @param mappings      The mappings for the template.
     * @param templateName  The name of the template.
     */
    public ESTemplate(EPatternRepository templatePath, Map<String, Object> mappings, String templateName) {
        this(templatePath, mappings, templateName, new ArrayList<ESTemplate>());
    }

    /**
     * Constructs an ESTemplate with the specified parameters.
     *
     * @param templatePath  The path of the template.
     * @param mappings      The mappings for the template.
     * @param templateName  The name of the template.
     * @param preconditionTemplates  The subTemplates associated with this template.
     */
    public ESTemplate(EPatternRepository templatePath, Map<String, Object> mappings, String templateName, ArrayList<ESTemplate> preconditionTemplates) {
        StringBuilder concatenatedQueries = new StringBuilder();

        for (ESTemplate precondition : preconditionTemplates) {
            concatenatedQueries.append(precondition.getEplQuery());
            concatenatedQueries.append("\n");
        }

        String loadedQuery = this.loadEplQuery(templatePath.getPath());
        this.est = concatenatedQueries + this.resolveTemplate(loadedQuery, mappings);
        this.templateName = templateName;
    }


    /**
     * Loads the EPL query from a file.
     *
     * @param filePath The path to the EPL file.
     * @return The contents of the EPL file as a String.
     */
    private String loadEplQuery(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException(STR."Failed to load EPL query from file: \{filePath}", e);
        }
    }

    /**
     * Resolves the EPL query template by replacing placeholders using the provided mappings.
     *
     * @param template The EPL template with placeholders.
     * @param mappings A map of placeholders and their corresponding values.
     * @return The resolved EPL query as a String.
     */
    private String resolveTemplate(String template, Map<String, Object> mappings) {

        Properties props = new Properties();
        props.setProperty("resource.loaders", "string");
        props.setProperty("resource.loader.string.class", "org.apache.velocity.runtime.resource.loader.StringResourceLoader");

        VelocityEngine velocityEngine = new VelocityEngine(props);
        velocityEngine.init();

        VelocityContext context = new VelocityContext();
        for (Map.Entry<String, Object> entry : mappings.entrySet()) {
            if (Objects.equals(entry.getKey(), "es_activities")) {

                Map<String, String> eventsMap = new HashMap<>();
                // Check if the value is an ArrayList
                if (entry.getValue() instanceof ArrayList) {
                    ArrayList<String> esActivities = (ArrayList<String>) entry.getValue();

                    int i = 0;
                    for (String activity : esActivities) {
                        eventsMap.put(String.format("activity_%d", i + 1), activity);
                        i++;
                    }
                }
                // Check if the value is a HashMap
                else if (entry.getValue() instanceof List) {
                    List<String> esActivities = (List<String>) entry.getValue();

                    int i = 0;
                    for (String activity : esActivities) {
                        eventsMap.put(String.format("activity_%d", i + 1), activity);
                        i++;
                    }

                } else {
                    eventsMap.put(entry.getKey(), entry.getValue().toString());
                }
                if(eventsMap.isEmpty()) {
                    System.err.println("eventsMap is empty");
                }
                context.put("eventsMap", eventsMap);
            }
            context.put(entry.getKey(), entry.getValue());
        }


        StringWriter writer = new StringWriter();

        velocityEngine.evaluate(context, writer, "EPL Template", template);

        return writer.toString();
    }

    public String getEplQuery() {
        return this.est;
    }

    @Override
    public String toString() {
        String[] lines = this.est.split("\\r?\\n");
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (String line : lines) {
            line = STR."\{index++}\t \{line} \n";
            builder.append(line);
        }
        return this.est;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public void saveToFile(String path) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(STR."rendered_\{path}.sql");
        out.println(this.toString());
        out.close();

    }
}

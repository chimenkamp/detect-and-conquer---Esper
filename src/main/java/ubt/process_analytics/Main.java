import com.opencsv.exceptions.CsvValidationException;
import ubt.process_analytics.esper.EPPMEventType;
import ubt.process_analytics.esper.ESProvider;
import ubt.process_analytics.esper.ESTemplate;

import ubt.process_analytics.utils.CSVProvider;
import ubt.process_analytics.utils.EPatternRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.io.FileWriter;

void main(String[] args) {

    String path = getCurrentEventLogPath();


    List<EPPMEventType> eventLog = loadEventLogFromDisc(path);

    ArrayList<ESTemplate> templates = new ArrayList<>();

    // getBaselineQueries(templates);

    ESTemplate missing_events = new ESTemplate(
            EPatternRepository.UNATTENDED_DECISION_POINTS,
            Map.ofEntries(
                    Map.entry("EPPMEventType", (Object) EPPMEventType.class.getName()),
                    Map.entry("time_window", (Object) "50 sec"),
                    Map.entry("positiveEvents", (Object) new ArrayList<String>() {{
                        add("Net loss reported");
                        add("Posting to balance sheet ");
                    }}),
                    Map.entry("negativeEvents", (Object) new ArrayList<String>() {{
                        add("Shareholder information updated");
                        add("News reported requested");
                    }})
            ),
            EPatternRepository.UNATTENDED_DECISION_POINTS.toString()
    );

    templates.add(missing_events);
    System.out.println(missing_events);
    System.exit(0);
    ESProvider esper = new ESProvider(templates);

    esper.setEnableHeuristicsMiner(false);
    esper.setLogComplexQueries(true);

    runEventLoop(esper, eventLog);

//    saveLatencyValuesToCsv(esper);


}

private static void saveLatencyValuesToCsv(ESProvider esper) {
    System.out.println(STR."After Loop: \{esper.getLatencyValues().size()}");

    List<Long> latencyValues = esper.getLatencyValues();
    System.out.println("Starting File Save");
    try {
        exportToCSV(latencyValues, "latency_values_1_queries.csv");
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private static void runEventLoop(ESProvider esper, List<EPPMEventType> eventLog) {
    while (true) {
        if (esper.getProcessEventCount() >= 2000000) {
            System.out.println(STR."In Loop: \{esper.getLatencyValues().size()}");
            System.exit(1);
            break;
        }
        for (EPPMEventType event : eventLog) {
            Instant start = Instant.now();
            ZoneId zone = ZoneId.of("Europe/Berlin");
            LocalDateTime date = LocalDateTime.ofInstant(start, zone);
            event.setTimestamp(date);
            esper.sendEvents(event);
        }
    }
}

private static List<EPPMEventType> loadEventLogFromDisc(String path) {
    List<EPPMEventType> eventLog;
    try {
        eventLog = CSVProvider.loadCSV(path);
    } catch (IOException | CsvValidationException e) {
        System.out.println("Could not load csv");
        e.printStackTrace();
        return null;
    }
    return eventLog;
}

private static String getCurrentEventLogPath() {
    // String path = STR."\{System.getProperty("user.dir")}/tests/datasets/Conditional and Event-Driven.csv";
    // String path = STR."\{System.getProperty("user.dir")}/tests/datasets/deadlock_and_conditional.csv";
    String path = STR."\{System.getProperty("user.dir")}/tests/datasets/Sepsis Cases - Event Log.csv";

    // Disable System.out.println
    // PrintStream originalOut = System.out;  // Save the original System.out
    // System.setOut(new PrintStream(new NullOutputStream()));

    // String path = STR."\{System.getProperty("user.dir")}/tests/datasets/Conditional and Event-Driven.csv";
    return path;
}

private void testTemplates() {
    ESTemplate missing_events = new ESTemplate(
            EPatternRepository.MISSING_EVENTS,
                Map.ofEntries(
                Map.entry("EPPMEventType", (Object) EPPMEventType.class.getName()),
                Map.entry("time_window", (Object) "50 sec"),
                Map.entry("es_activities", (Object) new ArrayList<String>() {{
                    add("Paper invoice received");
                }})
            ),
            EPatternRepository.MISSING_EVENTS.toString()
    );
    System.out.println(missing_events);
    System.exit(0);

}

public static void exportToCSV(List<Long> values, String fileName) throws IOException {
    try (FileWriter writer = new FileWriter(fileName)) {
        int index = 0;

        for (Long l : values) {
            writer.write(STR."\{index} \{l.toString()}  \n");
            index++;
            writer.flush();
        }
        System.out.println("Done");
    } catch (IOException e) {
        e.printStackTrace();
    }
}

public ArrayList<ESTemplate> loadAllTemplatesAsMock() throws FileNotFoundException {
    ArrayList<ESTemplate> templates = new ArrayList<>();
    for (EPatternRepository pattern: EPatternRepository.getAllPaths()) {
        ESTemplate tempTemplate= new ESTemplate(
                pattern,
                Map.ofEntries(
                        Map.entry("EPPMEventType", (Object) EPPMEventType.class.getName()),
                        Map.entry("time_window", (Object) "50 sec"),
                        Map.entry("es_activities", (Object) new ArrayList<String>() {{
                            add("Paper invoice received");
                            add("Electronic invoice received");
                        }})
                ),
                pattern.toString()
        );
        System.out.println(pattern.toString());
        tempTemplate.saveToFile(pattern.toString());
        templates.add(tempTemplate);
    }
    return templates;
}

private void getBaselineQueries(ArrayList<ESTemplate> templates) {

    ESTemplate xor= new ESTemplate(
        EPatternRepository.EXCLUSIVE_CHOICE,
        Map.ofEntries(
            Map.entry("EPPMEventType", (Object) EPPMEventType.class.getName()),
            Map.entry("time_window", (Object) "50 sec"),
            Map.entry("es_activities", (Object) new ArrayList<String>() {{
                add("Paper invoice received");
                add("Electronic invoice received");
            }})
        ),
        EPatternRepository.EXCLUSIVE_CHOICE.toString()
    );

    ESTemplate and = new ESTemplate(
        EPatternRepository.PARALLEL_SPLIT,
        Map.ofEntries(
                Map.entry("EPPMEventType", (Object) EPPMEventType.class.getName()),
                Map.entry("time_window", (Object) "50 sec"),
                Map.entry("es_activities", (Object) new ArrayList<String>() {{
                    add("Order is present");
                    add("Supplier is present");
                }})
        ),
        STR."\{EPatternRepository.PARALLEL_SPLIT}_\{0}"
    );
    templates.add(xor);
    templates.add(and);
}


// A custom OutputStream that does nothing
class NullOutputStream extends java.io.OutputStream {
    @Override
    public void write(int b) {
        // do nothing
    }
}
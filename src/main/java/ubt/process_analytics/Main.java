import com.opencsv.exceptions.CsvValidationException;
import ubt.process_analytics.esper.EPPMEventType;
import ubt.process_analytics.esper.ESProvider;
import ubt.process_analytics.esper.ESTemplate;
import ubt.process_analytics.utils.CSVProvider;
import ubt.process_analytics.utils.EPatternRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

void main(String[] args)  {

    // String path = "/tests/datasets/Conditional and Event-Driven.csv";
    String path = "/tests/datasets/deadlock_and_conditional.csv";
    // String path = "/tests/datasets/Sepsis Cases - Event Log.csv";

//    String path = "/tests/datasets/Subscriptions and Choices.csv";

    List<EPPMEventType> eventLog;
    try {
        eventLog = CSVProvider.loadCSV(path);
    } catch (IOException | CsvValidationException e) {
        System.out.println("Could not load csv");
        e.printStackTrace();
        return;
    }

    ArrayList<ESTemplate> templates = new ArrayList<>();

    ESTemplate xorStream= new ESTemplate(
            EPatternRepository.EXCLUSIVE_CHOICE_AS_STREAM,
            Map.ofEntries(
                    Map.entry("EPPMEventType", (Object) EPPMEventType.class.getName()),
                    Map.entry("time_window", (Object) "50 sec"),
                    Map.entry("es_activities", (Object) new ArrayList<String>() {{
                        add("Paper invoice received");
                        add("Electronic invoice received");
                        add("A");
                        add("B");
                        add("C");
                        add("D");
                    }})
            ),
            EPatternRepository.EXCLUSIVE_CHOICE_AS_STREAM.toString()
    );

    ESTemplate parallelMergeStream= new ESTemplate(
            EPatternRepository.PARALLEL_MERGE_AS_STREAM,
            Map.ofEntries(
                    Map.entry("EPPMEventType", (Object) EPPMEventType.class.getName()),
                    Map.entry("time_window", (Object) "50 sec"),
                    Map.entry("es_activities", (Object) new ArrayList<String>() {{
                        add("Clarification Sent to Supplier");
                        add("Order Amendment Confirmation");
                    }})
            ),
            EPatternRepository.PARALLEL_MERGE_AS_STREAM.toString()
    );

    ESTemplate deadlock = new ESTemplate(
            EPatternRepository.DEADLOCK,
            Map.ofEntries(
                    Map.entry("EPPMEventType", (Object) EPPMEventType.class.getName()),
                    Map.entry("time_window", (Object) "50 sec"),
                    Map.entry("es_activities", (Object) new ArrayList<String>() {{
                        add("Paper invoice received");
                        add("Electronic invoice received");
                        add("A");
                        add("B");
                    }})
            ),
            EPatternRepository.DEADLOCK.toString(),
            new ArrayList<ESTemplate>() {{
                add(xorStream);
                add(parallelMergeStream);
            }}
    );

    System.out.println(deadlock);


    templates.add(xorStream);
    templates.add(parallelMergeStream);
    templates.add(deadlock);


    ESProvider esper = new ESProvider(templates);
//    try {
//        loadAllTemplatesAsMock();
//    } catch (FileNotFoundException e) {
//        e.printStackTrace();
//    }
    int index = 0;

    for (EPPMEventType event : eventLog) {
        if (index < 20000) {
            esper.sendEvents(event);
        } else {
            break;
        }
        if (index % 1000 == 0) {
            System.out.println("Processed: " + index);
        }
        index++;
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
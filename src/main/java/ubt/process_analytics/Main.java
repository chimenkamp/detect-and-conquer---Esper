import com.opencsv.exceptions.CsvValidationException;
import ubt.process_analytics.esper.EPPMEventType;
import ubt.process_analytics.esper.ESProvider;
import ubt.process_analytics.esper.ESTemplate;
import ubt.process_analytics.utils.CSVProvider;
import ubt.process_analytics.utils.EPatternRepository;

import java.io.IOException;
import java.util.*;

void main(String[] args) {

    // String path = "/Users/christianimenkamp/Documents/Data-Repository/Conditional and Event-Driven.csv";
//    String path = "‘/Users/christianimenkamp/Documents/Data-Repository/deadlock_and_conditional.csv";
    String path = "/Users/christianimenkamp/Documents/Data-Repository/Community/sepsis/Sepsis Cases - Event Log.csv";


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
                    Map.entry("time_window", (Object) "10000 sec"),
                    Map.entry("es_activities", (Object) new ArrayList<String>() {{
                        add("Paper invoice received");
                        add("Electronic invoice received");
                    }})
            ),
            EPatternRepository.EXCLUSIVE_CHOICE_AS_STREAM.toString()
    );

//    ESTemplate parallelMergeStream= new ESTemplate(
//            EPatternRepository.PARALLEL_MERGE_AS_STREAM,
//            Map.ofEntries(
//                    Map.entry("EPPMEventType", (Object) EPPMEventType.class.getName()),
//                    Map.entry("time_window", (Object) "50 sec"),
//                    Map.entry("es_activities", (Object) new ArrayList<String>() {{
//                        add("Clarification Sent to Supplier");
//                        add("Order Amendment Confirmation");
//                    }})
//            ),
//            EPatternRepository.PARALLEL_MERGE_AS_STREAM.toString()
//    );
//    ESTemplate deadlock = new ESTemplate(
//            EPatternRepository.DEADLOCK,
//            Map.ofEntries(
//                    Map.entry("EPPMEventType", (Object) EPPMEventType.class.getName()),
//                    Map.entry("time_window", (Object) "10000 sec"),
//                    Map.entry("es_activities", (Object) new ArrayList<String>() {{
//                        add("Paper invoice received");
//                        add("Electronic invoice received");
//                        add("A");
//                        add("B");
//                    }})
//            ),
//            EPatternRepository.DEADLOCK.toString(),
//            new ArrayList<ESTemplate>() {{
//                add(xorStream);
//                add(parallelMergeStream);
//            }}
//    );
//
//    System.out.println(deadlock);


//    templates.add(xorStream);
//    templates.add(parallelMergeStream);
//    templates.add(deadlock);


    ESProvider esper = new ESProvider(templates);

    for (EPPMEventType event : eventLog) {
        esper.sendEvents(event);
    }

}


public ArrayList<ESTemplate> loadAllTemplates() {
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
                            add("A");
                            add("B");
                        }})
                ),
                pattern.toString()
        );
        templates.add(tempTemplate);
    }
    return templates;
}
package ubt.process_analytics.utils;

import ubt.process_analytics.esper.EPPMEventType;
import ubt.process_analytics.esper.ESTemplate;

import java.util.*;

public class LossyCountingHeuristicsMiner {
    private final double epsilon;
    private final int bucketWidth;
    private int currentBucket;
    private int eventCounter;

    // Data structures to store frequency of activities, relations, and control flow structures
    private final Map<String, FrequencyData> activities;
    private final Map<String, FrequencyData> relations;
    private final Map<String, FrequencyData> controlFlowStructures;
    private final Map<String, String> latestActivityForCase;

    public LossyCountingHeuristicsMiner(double epsilon) {
        this.epsilon = epsilon;
        this.bucketWidth = (int) Math.ceil(1 / epsilon);
        this.currentBucket = 1;
        this.eventCounter = 0;
        this.activities = new HashMap<>();
        this.relations = new HashMap<>();
        this.controlFlowStructures = new HashMap<>();
        this.latestActivityForCase = new HashMap<>();
    }

    /**
     * Adds a new event to the Lossy Counting HM.
     *
     * @param event The new event to be added.
     */
    public void addEvent(EPPMEventType event) {
        eventCounter++;

        // Determine the current bucket
        currentBucket = (int) Math.ceil((double) eventCounter / bucketWidth);

        // Update activity frequencies
        String activity = event.getActivity();
        activities.compute(activity, (key, value) -> {
            if (value == null) {
                return new FrequencyData(1, currentBucket - 1);
            } else {
                value.incrementFrequency();
                return value;
            }
        });

        // Update relations based on the latest activity for the case
        String caseID = event.getCaseID();
        if (latestActivityForCase.containsKey(caseID)) {
            String lastActivity = latestActivityForCase.get(caseID);
            String relation = lastActivity + "->" + activity;

            relations.compute(relation, (key, value) -> {
                if (value == null) {
                    return new FrequencyData(1, currentBucket - 1);
                } else {
                    value.incrementFrequency();
                    return value;
                }
            });

            // Detect and update control flow structures
            updateControlFlowStructures(caseID, lastActivity, activity);
        }

        // Update the latest activity for this case
        latestActivityForCase.put(caseID, activity);

        // Perform periodic cleanup
        if (eventCounter % bucketWidth == 0) {
            cleanup();
        }
    }
    /**
     * Updates control flow structures like Sequence, AND, and OR.
     *
     * @param caseID The case ID of the current event.
     * @param lastActivity The last activity executed for this case.
     * @param currentActivity The current activity being processed.
     */
    private void updateControlFlowStructures(String caseID, String lastActivity, String currentActivity) {

        String sequenceKey = STR."SEQ(\{lastActivity}->\{currentActivity})";
        controlFlowStructures.compute(sequenceKey, (key, value) -> {
            if (value == null) {
                return new FrequencyData(1, currentBucket - 1);
            } else {
                value.incrementFrequency();
                return value;
            }
        });

        String andKey = STR."AND(\{lastActivity} AND \{currentActivity})";
        controlFlowStructures.compute(andKey, (key, value) -> {
            if (value == null) {
                return new FrequencyData(1, currentBucket - 1);
            } else {
                value.incrementFrequency();
                return value;
            }
        });

        String orKey = STR."OR(\{lastActivity} OR \{currentActivity})";
        controlFlowStructures.compute(orKey, (key, value) -> {
            if (value == null) {
                return new FrequencyData(1, currentBucket - 1);
            } else {
                value.incrementFrequency();
                return value;
            }
        });
    }
    /**
     * Performs the periodic cleanup of the data structures.
     * Removes entries with a frequency that, when added to the maximum error, is less than the current bucket.
     */
    private void cleanup() {
        // Cleanup activities
        Iterator<Map.Entry<String, FrequencyData>> activityIterator = activities.entrySet().iterator();
        while (activityIterator.hasNext()) {
            Map.Entry<String, FrequencyData> entry = activityIterator.next();
            if (entry.getValue().getFrequency() + entry.getValue().getDelta() <= currentBucket) {
                activityIterator.remove();
            }
        }

        // Cleanup relations
        Iterator<Map.Entry<String, FrequencyData>> relationIterator = relations.entrySet().iterator();
        while (relationIterator.hasNext()) {
            Map.Entry<String, FrequencyData> entry = relationIterator.next();
            if (entry.getValue().getFrequency() + entry.getValue().getDelta() <= currentBucket) {
                relationIterator.remove();
            }
        }

        // Cleanup control flow structures
        Iterator<Map.Entry<String, FrequencyData>> controlFlowIterator = controlFlowStructures.entrySet().iterator();
        while (controlFlowIterator.hasNext()) {
            Map.Entry<String, FrequencyData> entry = controlFlowIterator.next();
            if (entry.getValue().getFrequency() + entry.getValue().getDelta() <= currentBucket) {
                controlFlowIterator.remove();
            }
        }
    }


    /**
     * Gets the current frequency of activities.
     *
     * @return A map of activity names to their frequency counts.
     */
    public Map<String, Integer> getActivityFrequencies() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, FrequencyData> entry : activities.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getFrequency());
        }
        return result;
    }

    /**
     * Gets the current frequency of relations between activities.
     *
     * @return A map of relations (e.g., "A->B") to their frequency counts.
     */
    public Map<String, Integer> getRelationFrequencies() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, FrequencyData> entry : relations.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getFrequency());
        }
        return result;
    }

    /**
     * Gets the current frequency of control flow structures.
     *
     * @return A map of control flow structure names to their frequency counts.
     */
    public Map<String, Integer> getControlFlowFrequencies() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, FrequencyData> entry : controlFlowStructures.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getFrequency());
        }
        return result;
    }

    /**
     * Converts detected patterns into ESTemplate objects.
     *
     * @return A list of ESTemplate objects representing the detected patterns.
     */
    public List<ESTemplate> convertToTemplates() {
        List<ESTemplate> templates = new ArrayList<>();

        // Iterate over detected control flow structures
        for (Map.Entry<String, FrequencyData> entry : controlFlowStructures.entrySet()) {
            String structure = entry.getKey();
            String[] elements;

            // Handle XOR (Exclusive Choice)
            if (structure.startsWith("OR(")) {
                elements = extractElementsFromStructure(structure);
                templates.add(createXORTemplate(elements));
            }

            // Handle Sequence
            else if (structure.startsWith("SEQ(")) {
                elements = extractElementsFromStructure(structure);

                templates.add(createSequenceTemplate(elements));
            }

            // Handle AND
            else if (structure.startsWith("AND(")) {
                elements = extractElementsFromStructure(structure);
                templates.add(createANDTemplate(elements));
            }
        }

        return templates;
    }

    /**
     * Creates an XOR (Exclusive Choice) template.
     *
     * @param elements The elements involved in the XOR pattern.
     * @return An ESTemplate representing the XOR pattern.
     */
    private ESTemplate createXORTemplate(String[] elements) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("EPPMEventType", EPPMEventType.class.getName());
        parameters.put("time_window", "10000 sec");
        parameters.put("es_activities", List.of(elements));

        return new ESTemplate(
                EPatternRepository.EXCLUSIVE_CHOICE,
                parameters,
                STR."Exclusive Choice between \{String.join(", ", elements)}"
        );
    }

    /**
     * Creates a Sequence template.
     *
     * @param elements The elements involved in the Sequence pattern.
     * @return An ESTemplate representing the Sequence pattern.
     */
    private ESTemplate createSequenceTemplate(String[] elements) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("EPPMEventType", EPPMEventType.class.getName());
        parameters.put("time_window", "10000 sec");
        parameters.put("es_activities", List.of(elements));

        return new ESTemplate(
                EPatternRepository.C_5_MULTI_EVENT_TRIGGER,
                parameters,
                STR."Sequence of \{String.join(" -> ", elements)}"
        );
    }

    /**
     * Creates an AND template.
     *
     * @param elements The elements involved in the AND pattern.
     * @return An ESTemplate representing the AND pattern.
     */
    private ESTemplate createANDTemplate(String[] elements) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("EPPMEventType", EPPMEventType.class.getName());
        parameters.put("time_window", "10000 sec");
        parameters.put("es_activities", List.of(elements));

        return new ESTemplate(
                EPatternRepository.PARALLEL_SPLIT,
                parameters,
                "AND between " + String.join(" and ", elements)
        );
    }

    /**
     * Extracts the elements from a control flow structure string.
     *
     * @param structure The control flow structure string.
     * @return An array of elements involved in the structure.
     */
    private String[] extractElementsFromStructure(String structure) {
        String elementsPart = structure.substring(structure.indexOf('(') + 1, structure.indexOf(')'));
        return elementsPart.split(" OR | AND |->");
    }


    /**
     * A helper class to store frequency data with the associated delta (error).
     */
    private static class FrequencyData {
        private int frequency;
        private final int delta;

        public FrequencyData(int frequency, int delta) {
            this.frequency = frequency;
            this.delta = delta;
        }

        public void incrementFrequency() {
            this.frequency++;
        }

        public int getFrequency() {
            return frequency;
        }

        public int getDelta() {
            return delta;
        }
    }
}

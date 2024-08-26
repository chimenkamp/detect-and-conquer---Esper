package ubt.process_analytics.esper;

import ubt.process_analytics.utils.LossyCountingHeuristicsMiner;
import ubt.process_analytics.utils.PROBS;

import java.util.List;
import java.util.Map;

public class EPStreamMetrics {
    private long eventCount;
    private long startTime;
    private long complexEventCount;
    private final PROBS config = PROBS.getInstance();

    /**
     * Constructor to initialize the metrics tracker.
     */
    public EPStreamMetrics(LossyCountingHeuristicsMiner hm, boolean print_heuristic_results) {
        reset();

        new Thread(() -> {
            while (true) {
                try {

                    Thread.sleep(this.config.getInt("ESPER_CONFIG_LOG_METRICS_IN_MS")); // 10 seconds
                    this.printMetrics();
                    if (print_heuristic_results) {
                        Map<String, Integer> activityFrequencies = hm.getActivityFrequencies();
                        Map<String, Integer> relationFrequencies = hm.getRelationFrequencies();
                        Map<String, Integer> controlFlowFrequencies = hm.getControlFlowFrequencies();

                        System.out.println("Activity Frequencies:"+ activityFrequencies);
                        System.out.println("Relation Frequencies:"+ relationFrequencies);
                        System.out.println("Control Flow:"+ controlFlowFrequencies);

                        // count the controlFlowFrequencies keys with a substring
                        int orCounter = 0;
                        int andCounter = 0;
                        int seqCounter = 0;
                        for (String entry : controlFlowFrequencies.keySet()) {
                            if(entry.contains("OR")) {
                               orCounter++;
                            }
                            if(entry.contains("AND")) {
                                andCounter++;
                            }
                            if(entry.contains("SEQ")) {
                                seqCounter++;
                            }
                        }
                        System.out.println(STR."OR Counter: \{orCounter} AND Counter: \{andCounter} SEQ: \{seqCounter}");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Resets the metrics.
     */
    public synchronized void reset() {
        this.eventCount = 0;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Increment the event count.
     */
    public synchronized void incrementEventCount() {
        this.eventCount++;
    }

    /**
     *  Increment complex event count
     */
    public synchronized void incrementComplexEventCount() {
        this.complexEventCount++;
    }

    /**
     * Gets the events per second (EPS).
     *
     * @return The current EPS.
     */
    public synchronized double getEventsPerSecond() {
        long elapsedTime = System.currentTimeMillis() - this.startTime;
        return (elapsedTime > 0) ? (this.eventCount / (elapsedTime / 1000.0)) : 0;
    }

    /**
     * Gets the total number of events.
     *
     * @return The total number of events.
     */
    public synchronized long getTotalEvents() {
        return this.eventCount;
    }

    /**
     * Gets the total number of events.
     *
     * @return The total number of events.
     */
    public synchronized long getTotalComplexEvents() {
        return this.complexEventCount;
    }

    public void printMetrics() {
        System.out.println("----------");
        System.out.println(STR."Events per second: \{this.getEventsPerSecond()}");
        System.out.println(STR."Total Events: \{this.getTotalEvents()}");
        System.out.println(STR."Total Complex Events: \{this.getTotalComplexEvents()}");
        System.out.println("----------");
    }
}

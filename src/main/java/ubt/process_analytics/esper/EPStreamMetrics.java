package ubt.process_analytics.esper;

import ubt.process_analytics.utils.PROBS;

public class EPStreamMetrics {
    private long eventCount;
    private long startTime;
    private long complexEventCount;
    private final PROBS config = PROBS.getInstance();
    /**
     * Constructor to initialize the metrics tracker.
     */
    public EPStreamMetrics() {
        reset();

        new Thread(() -> {
            while (true) {
                try {

                    Thread.sleep(this.config.getInt("ESPER_CONFIG_LOG_METRICS_IN_MS")); // 10 seconds
                    this.printMetrics();

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

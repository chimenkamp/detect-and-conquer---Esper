package ubt.process_analytics.utils;

import java.util.ArrayList;
import java.util.List;

public class PerformanceEvaluator {
    private long eventCount;
    private long startTime;
    private long totalLatency;

    private final List<Long> latencyValues;
    private final List<Double> throughputValues;
    private final List<Long> timestamps;

    /**
     * Initializes a new PerformanceEvaluator instance.
     */
    public PerformanceEvaluator() {
        this.eventCount = 0;
        this.startTime = 0;
        this.totalLatency = 0;

        this.latencyValues = new ArrayList<>();
        this.throughputValues = new ArrayList<>();
        this.timestamps = new ArrayList<>();
    }

    /**
     * Records the start time when the first event in a window is ingested.
     */
    public void recordStartTime() {
        this.startTime = System.nanoTime();
    }

    /**
     * Calculates latency for a processed event and updates the metrics.
     */
    public void recordEventProcessed() {
        long currentTime = System.nanoTime();
        long latency = currentTime - this.startTime;
        this.totalLatency += latency;
        this.eventCount++;

        // Store current metrics for plotting
        latencyValues.add(latency);
        throughputValues.add(getThroughput());
        timestamps.add(System.currentTimeMillis());
    }

    /**
     * Calculates and returns the average latency in milliseconds.
     *
     * @return The average latency in milliseconds.
     */
    public double getAverageLatency() {
        if (this.eventCount == 0) {
            return 0;
        }
        // Convert nanoseconds to milliseconds
        return (this.totalLatency / (double) this.eventCount) / 1_000_000;
    }

    /**
     * Calculates and returns the throughput in events per second.
     *
     * @return The throughput in events per second.
     */
    public double getThroughput() {
        if (this.startTime == 0) {
            return 0;
        }
        long elapsedTime = System.nanoTime() - this.startTime; // in nanoseconds
        double elapsedTimeInSeconds = elapsedTime / 1_000_000_000.0; // convert to seconds
        return this.eventCount / elapsedTimeInSeconds;
    }

    /**
     * Resets the performance metrics.
     */
    public void reset() {
        this.eventCount = 0;
        this.startTime = 0;
        this.totalLatency = 0;

        latencyValues.clear();
        throughputValues.clear();
        timestamps.clear();
    }

    /**
     * Returns the latency values for plotting.
     *
     * @return A list of latency values in nanoseconds.
     */
    public List<Long> getLatencyValues() {
        return new ArrayList<>(latencyValues);
    }

    /**
     * Returns the throughput values for plotting.
     *
     * @return A list of throughput values in events per second.
     */
    public List<Double> getThroughputValues() {
        return new ArrayList<>(throughputValues);
    }

    /**
     * Returns the timestamps for plotting.
     *
     * @return A list of timestamps in milliseconds.
     */
    public List<Long> getTimestamps() {
        return new ArrayList<>(timestamps);
    }
}

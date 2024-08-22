package ubt.process_analytics.utils;

public enum EConstants {
    KEY_TIMESTAMP("time:timestamp"),
    KEY_ACTIVITY("concept:name"),
    KEY_CASE_ID("case:concept:name");

    private final String value;

    /**
     * Constructor for the enum that accepts a string value.
     *
     * @param value The string value associated with the enum constant.
     */
    EConstants(String value) {
        this.value = value;
    }

    /**
     * Retrieves the string value associated with the enum constant.
     *
     * @return The string value of the enum constant.
     */
    public String getValue() {
        return value;
    }
}

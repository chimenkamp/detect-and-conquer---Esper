package ubt.process_analytics.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EPatternRepository {
    BASE_PATH(STR."\{System.getProperty("user.dir")}/src/main/resources/epl/"),
    BASIC_CONTROL_FLOW_PATTERNS_PATH(STR."\{BASE_PATH.getPath()}basic-control-flow-patterns/"),
    CONTROL_FLOW_ANTI_PATTERNS_PATH(STR."\{BASE_PATH.getPath()}control-flow-anti-patterns/"),
    INSTANTIATION_SEMANTICS_PATH(STR."\{BASE_PATH.getPath()}instantiation-semantics/"),

    // Basic Control Flow Patterns
    EXCLUSIVE_CHOICE(STR."\{BASIC_CONTROL_FLOW_PATTERNS_PATH.getPath()}exclusive_choice.epl"),
    EXCLUSIVE_CHOICE_AS_STREAM(STR."\{BASIC_CONTROL_FLOW_PATTERNS_PATH.getPath()}exclusive_choice_as_stream.sql"),
    PARALLEL_SPLIT(STR."\{BASIC_CONTROL_FLOW_PATTERNS_PATH.getPath()}parallel_split.sql"),
    PARALLEL_MERGE_AS_STREAM(STR."\{BASIC_CONTROL_FLOW_PATTERNS_PATH.getPath()}parallel_merge_as_stream.sql"),
    // RAW_TEST(BASIC_CONTROL_FLOW_PATTERNS_PATH.getPath() + "raw_test.sql"),


    // Control Flow Anti-Patterns
    DEADLOCK(STR."\{CONTROL_FLOW_ANTI_PATTERNS_PATH.getPath()}deadlock.sql"),
    INFINITE_LOOP(STR."\{CONTROL_FLOW_ANTI_PATTERNS_PATH.getPath()}Infinite Loop.sql"),


    // Instantiation Semantics
    A_4_OCCURRED_EVENTS(STR."\{INSTANTIATION_SEMANTICS_PATH.getPath()}A-4 Occurred Events.sql"),
    C_4_SINGLE_EVENT_TRIGGER(STR."\{INSTANTIATION_SEMANTICS_PATH.getPath()}C-4 Single Event Trigger.sql"),
    C_5_MULTI_EVENT_TRIGGER(STR."\{INSTANTIATION_SEMANTICS_PATH.getPath()}C-5 Multi Event Trigger.sql"),
    S_1_ALL_SUBSCRIPTIONS(STR."\{INSTANTIATION_SEMANTICS_PATH.getPath()}S-1 All Subscriptions.sql"),
    S_2_NO_SUBSCRIPTIONS(STR."\{INSTANTIATION_SEMANTICS_PATH.getPath()}S-2 No Subscriptions.sql"),
    S_3_REACHABLE_SUBSCRIPTION(STR."\{INSTANTIATION_SEMANTICS_PATH.getPath()}S-3 Reachable Subscription.sql"),
    U_1_UNTIL_CONSUMPTION(STR."\{INSTANTIATION_SEMANTICS_PATH.getPath()}U-1 Until Consumption.sql"),
    U_4_EVENT_BASED_UNSUBSCRIPTION(STR."\{INSTANTIATION_SEMANTICS_PATH.getPath()}U-4 Event-based Unsubscription.sql");

    private final String path;

    /**
     * Constructor for SqlFilePath enum.
     *
     * @param path Path to the SQL file.
     */
    EPatternRepository(String path) {
        this.path = path;
    }

    /**
     * Get the path associated with the enum constant.
     *
     * @return Path as a string.
     */
    public String getPath() {
        return path;
    }

    /**
     * Get a list of enum constants that represent real SQL files.
     *
     * @return List of EPatternRepository constants representing SQL files.
     */
    public static List<EPatternRepository> getAllPaths() {
        return Arrays.stream(EPatternRepository.values())
                .filter(e -> !e.name().endsWith("_PATH") && !e.name().equals("BASE_PATH"))
                .collect(Collectors.toList());
    }

}

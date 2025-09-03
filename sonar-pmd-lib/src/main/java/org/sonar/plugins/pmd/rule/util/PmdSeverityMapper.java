package org.sonar.plugins.pmd.rule.util;

/**
 * Utility to map PMD priority levels (1..5 as strings) to Sonar severity labels.
 */
public final class PmdSeverityMapper {

    private PmdSeverityMapper() {
        // utility class
    }

    /**
     * Converts PMD priority ("1".."5") into Sonar severity string.
     * Defaults to "MAJOR" when the input is null/empty/unknown.
     *
     * PMD priorities:
     * 1 -> BLOCKER
     * 2 -> CRITICAL
     * 3 -> MAJOR
     * 4 -> MINOR
     * 5 -> INFO
     *
     * @param priority PMD priority as string
     * @return Sonar severity label
     */
    public static String priorityToSeverity(String priority) {
        if (priority == null) {
            return "MAJOR";
        }
        switch (priority.trim()) {
            case "1": return "BLOCKER";
            case "2": return "CRITICAL";
            case "3": return "MAJOR";
            case "4": return "MINOR";
            case "5": return "INFO";
            default: return "MAJOR";
        }
    }
}

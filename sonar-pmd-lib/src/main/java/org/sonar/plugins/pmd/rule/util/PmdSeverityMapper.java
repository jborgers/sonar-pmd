package org.sonar.plugins.pmd.rule.util;

import org.sonar.api.rule.Severity;

/**
 * Utility to map PMD priority levels (1..5 as strings) to Sonar severity labels.
 */
public final class PmdSeverityMapper {

    private PmdSeverityMapper() {
        // utility class
    }

    /**
     * Converts PMD priority ("1".."5") into Sonar severity string.
     *
     * @param pmdPriority PMD priority as string
     * @param category    Category of the PMD rule
     * @return Sonar severity as defined by {@code org.sonar.api.rule.Severity} (BLOCKER, CRITICAL, MAJOR, MINOR, INFO).
     * Defaults to "MAJOR" when the input is null/empty/unknown.
     * <p>
     * Default Priority Mapping:
     * For standard PMD rules, priorities are mapped as follows:
     * <ul>
     *   <li>1 &rarr; BLOCKER</li>
     *   <li>2 &rarr; CRITICAL (aka HIGH)</li>
     *   <li>3 &rarr; MAJOR (aka MEDIUM)</li>
     *   <li>4 &rarr; MINOR (aka LOW)</li>
     *   <li>5 &rarr; MINOR (aka LOW)</li>
     * </ul>
     * <p>
     * Code Style Priority Mapping:
     * For PMD rules in the code style category, severities are reduced using this mapping:
     * <ul>
     *   <li>1, 2 &rarr; MAJOR (aka MEDIUM)</li>
     *   <li>3, 4, 5 &rarr; MINOR (aka LOW)</li>
     * </ul>
     * <p>
     * Notes:
     * <ul>
     *   <li>Recent Sonar renaming (MQR mode): CRITICAL &rarr; HIGH, MAJOR &rarr; MEDIUM, MINOR &rarr; LOW</li>
     *   <li>Deprecated API {@code org.sonar.check.Priority} - use {@code org.sonar.api.rule.Severity} instead</li>
     *   <li>Do not use {@code org.sonar.api.issue.impact.Severity} - contains LOW, MEDIUM, HIGH</li>
     * </ul>
     */
    public static String priorityToSeverity(String pmdPriority, String category) {
        if (pmdPriority == null) {
            return Severity.defaultSeverity();
        }
        if (category != null && category.equals("codestyle")) {
            switch (pmdPriority.trim()) {
                case "1":
                case "2":
                    return Severity.MAJOR;
                case "3":
                case "4":
                case "5":
                    return Severity.MINOR;
                default:
                    return Severity.defaultSeverity();
            }
        }
        else {
            switch (pmdPriority.trim()) {
                case "1":
                    return Severity.BLOCKER;
                case "2":
                    return Severity.CRITICAL;
                case "3":
                    return Severity.MAJOR;
                case "4":
                case "5":
                    return Severity.MINOR;
                default:
                    return Severity.defaultSeverity();
            }
        }
    }
}

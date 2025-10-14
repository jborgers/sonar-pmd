/*
 * Shared PmdPriorities moved to lib to reduce duplication across modules.
 */
package org.sonar.plugins.pmd;

import java.util.Locale;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.pmd.xml.PmdRule;

public final class PmdPriorities {

    private static final int NUM_SEVERITIES = Severity.ALL.size();
    private PmdPriorities() {
        // only static methods
    }

    public static org.sonar.api.rules.RulePriority sonarPrioOf(@NotNull PmdRule pmdRule) {
        return toSonarPrio(pmdRule.getPriority());
    }

    public static org.sonar.api.rules.RulePriority toSonarPrio(@Nullable Integer pmdPrioLevel) {
        String severity = toSonarSeverity(pmdPrioLevel);
        return (severity != null) ? RulePriority.valueOf(severity) : null;
    }

    public static String toSonarSeverity(@Nullable Integer pmdPrioLevel) {
        if (Objects.isNull(pmdPrioLevel)) {
            return null;
        }
        final int index = Math.abs(NUM_SEVERITIES - pmdPrioLevel);
        return (index < NUM_SEVERITIES) ? Severity.ALL.get(index) : null;
    }

    public static Integer fromSonarPrio(org.sonar.api.rules.RulePriority priority) {
        return Math.abs(priority.ordinal() - NUM_SEVERITIES);
    }

    public static Integer fromSonarSeverity(@NotNull String severity) {
        return Math.abs(NUM_SEVERITIES - Severity.ALL.indexOf(severity));
    }

    public static Integer ofSonarRule(@NotNull ActiveRule sonarRule) {
        return fromSonarSeverity(sonarRule.severity().toUpperCase(Locale.ENGLISH));
    }
}

package org.sonar.plugins.pmd;

import org.sonar.api.batch.rule.ActiveRule;

public final class PmdPriorities {

    private PmdPriorities() {
    }

    public static int ofSonarRule(ActiveRule rule) {
        String sev = rule.severity();
        if (sev == null) {
            return 3;
        }
        switch (sev) {
            case "BLOCKER":
                return 1;
            case "CRITICAL":
                return 2;
            case "MAJOR":
                return 3;
            case "MINOR":
                return 4;
            case "INFO":
                return 5;
            default:
                return 3;
        }
    }
}

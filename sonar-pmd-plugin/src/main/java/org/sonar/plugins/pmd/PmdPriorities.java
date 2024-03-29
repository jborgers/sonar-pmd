/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2021 SonarSource SA and others
 * mailto:jborgers AT jpinpoint DOT com; peter.paul.bakker AT stokpop DOT nl
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.pmd;

import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.pmd.xml.PmdRule;

public final class PmdPriorities {

    private static final int NUM_SEVERITIES = Severity.ALL.size();
    private PmdPriorities() {
        // only static methods
    }

    public static org.sonar.api.rules.RulePriority sonarPrioOf(@Nonnull PmdRule pmdRule) {
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

    public static Integer fromSonarSeverity(@Nonnull String severity) {
        return Math.abs(NUM_SEVERITIES - Severity.ALL.indexOf(severity));
    }

    public static Integer ofSonarRule(@Nonnull ActiveRule sonarRule) {
        return fromSonarSeverity(sonarRule.severity().toUpperCase(Locale.ENGLISH));
    }
}

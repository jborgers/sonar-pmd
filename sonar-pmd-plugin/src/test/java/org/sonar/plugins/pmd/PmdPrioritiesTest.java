/*
 * SonarQube PMD7 Plugin
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

import org.junit.jupiter.api.Test;
import org.sonar.api.rule.Severity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.api.rules.RulePriority.BLOCKER;
import static org.sonar.api.rules.RulePriority.CRITICAL;
import static org.sonar.api.rules.RulePriority.INFO;
import static org.sonar.api.rules.RulePriority.MAJOR;
import static org.sonar.api.rules.RulePriority.MINOR;

class PmdPrioritiesTest {

    @Test
    void should_get_priority_from_level() {
        assertThat(PmdPriorities.toSonarPrio(1)).isSameAs(BLOCKER);
        assertThat(PmdPriorities.toSonarPrio(2)).isSameAs(CRITICAL);
        assertThat(PmdPriorities.toSonarPrio(3)).isSameAs(MAJOR);
        assertThat(PmdPriorities.toSonarPrio(4)).isSameAs(MINOR);
        assertThat(PmdPriorities.toSonarPrio(5)).isSameAs(INFO);
        assertThat(PmdPriorities.toSonarPrio(-1)).isNull();
        assertThat(PmdPriorities.toSonarPrio(null)).isNull();
    }

    @Test
    void should_get_priority_from_level_severity() {
        assertThat(PmdPriorities.toSonarSeverity(1)).isSameAs(Severity.BLOCKER);
        assertThat(PmdPriorities.toSonarSeverity(2)).isSameAs(Severity.CRITICAL);
        assertThat(PmdPriorities.toSonarSeverity(3)).isSameAs(Severity.MAJOR);
        assertThat(PmdPriorities.toSonarSeverity(4)).isSameAs(Severity.MINOR);
        assertThat(PmdPriorities.toSonarSeverity(5)).isSameAs(Severity.INFO);
        assertThat(PmdPriorities.toSonarSeverity(-1)).isNull();
        assertThat(PmdPriorities.toSonarSeverity(null)).isNull();
    }

    @Test
    void should_get_level_from_priority() {
        assertThat(PmdPriorities.fromSonarPrio(BLOCKER)).isEqualTo(1);
        assertThat(PmdPriorities.fromSonarPrio(CRITICAL)).isEqualTo(2);
        assertThat(PmdPriorities.fromSonarPrio(MAJOR)).isEqualTo(3);
        assertThat(PmdPriorities.fromSonarPrio(MINOR)).isEqualTo(4);
        assertThat(PmdPriorities.fromSonarPrio(INFO)).isEqualTo(5);
    }

    @Test
    void should_get_level_from_priority_severity() {
        assertThat(PmdPriorities.fromSonarSeverity(Severity.BLOCKER)).isEqualTo(1);
        assertThat(PmdPriorities.fromSonarSeverity(Severity.CRITICAL)).isEqualTo(2);
        assertThat(PmdPriorities.fromSonarSeverity(Severity.MAJOR)).isEqualTo(3);
        assertThat(PmdPriorities.fromSonarSeverity(Severity.MINOR)).isEqualTo(4);
        assertThat(PmdPriorities.fromSonarSeverity(Severity.INFO)).isEqualTo(5);
    }
}

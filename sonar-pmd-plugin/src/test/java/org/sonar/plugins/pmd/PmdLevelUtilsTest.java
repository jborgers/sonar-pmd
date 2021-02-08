/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2021 SonarSource SA and others
 * mailto:jens AT gerdes DOT digital
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.api.rules.RulePriority.BLOCKER;
import static org.sonar.api.rules.RulePriority.CRITICAL;
import static org.sonar.api.rules.RulePriority.INFO;
import static org.sonar.api.rules.RulePriority.MAJOR;
import static org.sonar.api.rules.RulePriority.MINOR;

class PmdLevelUtilsTest {
    @Test
    void should_get_priority_from_level() {
        assertThat(PmdLevelUtils.fromLevel(1)).isSameAs(BLOCKER);
        assertThat(PmdLevelUtils.fromLevel(2)).isSameAs(CRITICAL);
        assertThat(PmdLevelUtils.fromLevel(3)).isSameAs(MAJOR);
        assertThat(PmdLevelUtils.fromLevel(4)).isSameAs(MINOR);
        assertThat(PmdLevelUtils.fromLevel(5)).isSameAs(INFO);
        assertThat(PmdLevelUtils.fromLevel(-1)).isNull();
        assertThat(PmdLevelUtils.fromLevel(null)).isNull();
    }

    @Test
    void should_get_level_from_priority() {
        assertThat(PmdLevelUtils.toLevel(BLOCKER)).isEqualTo(1);
        assertThat(PmdLevelUtils.toLevel(CRITICAL)).isEqualTo(2);
        assertThat(PmdLevelUtils.toLevel(MAJOR)).isEqualTo(3);
        assertThat(PmdLevelUtils.toLevel(MINOR)).isEqualTo(4);
        assertThat(PmdLevelUtils.toLevel(INFO)).isEqualTo(5);
    }
}

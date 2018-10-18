/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;
import org.sonar.api.rules.RulePriority;

import static org.assertj.core.api.Assertions.assertThat;

class PmdLevelUtilsTest {
    @Test
    void should_get_priority_from_level() {
        assertThat(PmdLevelUtils.fromLevel("1")).isSameAs(RulePriority.BLOCKER);
        assertThat(PmdLevelUtils.fromLevel("2")).isSameAs(RulePriority.CRITICAL);
        assertThat(PmdLevelUtils.fromLevel("3")).isSameAs(RulePriority.MAJOR);
        assertThat(PmdLevelUtils.fromLevel("4")).isSameAs(RulePriority.MINOR);
        assertThat(PmdLevelUtils.fromLevel("5")).isSameAs(RulePriority.INFO);
        assertThat(PmdLevelUtils.fromLevel("?")).isNull();
        assertThat(PmdLevelUtils.fromLevel(null)).isNull();
    }

    @Test
    void should_get_level_from_priority() {
        assertThat(PmdLevelUtils.toLevel(RulePriority.BLOCKER)).isEqualTo("1");
        assertThat(PmdLevelUtils.toLevel(RulePriority.CRITICAL)).isEqualTo("2");
        assertThat(PmdLevelUtils.toLevel(RulePriority.MAJOR)).isEqualTo("3");
        assertThat(PmdLevelUtils.toLevel(RulePriority.MINOR)).isEqualTo("4");
        assertThat(PmdLevelUtils.toLevel(RulePriority.INFO)).isEqualTo("5");
    }

    @Test
    void private_constructor() throws Exception {
        Constructor constructor = PmdLevelUtils.class.getDeclaredConstructor();
        assertThat(constructor.isAccessible()).isFalse();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}

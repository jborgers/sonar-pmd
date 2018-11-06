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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonar.api.rules.RulePriority;

public final class PmdLevelUtils {

    private static final Map<RulePriority, String> PRIORITY_TO_LEVEL = preparePriorityToLevel();
    private static final Map<String, RulePriority> LEVEL_TO_PRIORITY = prepareLevelToPriority();

    private static Map<RulePriority, String> preparePriorityToLevel() {
        final Map<RulePriority, String> map = new HashMap<>();
        map.put(RulePriority.BLOCKER, "1");
        map.put(RulePriority.CRITICAL, "2");
        map.put(RulePriority.MAJOR, "3");
        map.put(RulePriority.MINOR, "4");
        map.put(RulePriority.INFO, "5");

        return map;
    }

    private static Map<String, RulePriority> prepareLevelToPriority() {
        final Map<String, RulePriority> map = new HashMap<>();
        preparePriorityToLevel().forEach((key, value) -> map.put(value, key));

        return map;
    }

    private PmdLevelUtils() {
        // only static methods
    }

    public static RulePriority fromLevel(@Nullable String level) {
        return LEVEL_TO_PRIORITY.get(level);
    }

    public static String toLevel(RulePriority priority) {
        return PRIORITY_TO_LEVEL.get(priority);
    }
}

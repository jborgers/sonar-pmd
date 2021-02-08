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

import java.util.Objects;
import javax.annotation.Nullable;

import org.sonar.api.rules.RulePriority;

public final class PmdLevelUtils {

    private static final int INDEX_LEVEL = RulePriority.values().length;
    private PmdLevelUtils() {
        // only static methods
    }

    public static RulePriority fromLevel(@Nullable Integer level) {

        if (Objects.isNull(level)) {
            return null;
        }

        final int index = Math.abs(INDEX_LEVEL - level);

        return (index < INDEX_LEVEL) ? RulePriority.valueOfInt(index) : null;
    }

    public static Integer toLevel(RulePriority priority) {
        return Math.abs(priority.ordinal() - INDEX_LEVEL);
    }
}

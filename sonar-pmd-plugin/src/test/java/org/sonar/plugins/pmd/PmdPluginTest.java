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
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.plugins.pmd.rule.PmdKotlinRulesDefinition;
import org.sonar.plugins.pmd.rule.PmdRulesDefinition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class PmdPluginTest {

    private final PmdPlugin subject = new PmdPlugin();

    @SuppressWarnings("unchecked")
    @Test
    void testPluginConfiguration() {
        // given
        final SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(7, 3), SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);
        final Plugin.Context context = new Plugin.Context(runtime);

        // when
        subject.define(context);

        // then
        final List extensions = context.getExtensions();
        assertThat(extensions)
                .hasSize(8)
                .contains(
                        PmdSensor.class,
                        PmdConfiguration.class,
                        PmdExecutor.class,
                        PmdRulesDefinition.class,
                        PmdKotlinRulesDefinition.class,
                        PmdViolationRecorder.class
                );
    }

    // TODO Compare expected classes with all classes annotated with ScannerSide annotation.
}

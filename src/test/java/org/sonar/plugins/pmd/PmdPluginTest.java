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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.plugins.pmd.profile.PmdProfileExporter;
import org.sonar.plugins.pmd.profile.PmdProfileImporter;
import org.sonar.plugins.pmd.rule.PmdRulesDefinition;
import org.sonar.plugins.pmd.rule.PmdUnitTestsRulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;


class PmdPluginTest {

    private final PmdPlugin subject = new PmdPlugin();

    @SuppressWarnings("unchecked")
    @Test
    void testPluginConfiguration() {
        final SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(7, 3), SonarQubeSide.SCANNER);
        final Plugin.Context context = new Plugin.Context(runtime);

        subject.define(context);
        final List extensions = context.getExtensions();
        assertThat(extensions).hasSize(9);
        assertThat(extensions).contains(
                PmdSensor.class,
                PmdConfiguration.class,
                PmdExecutor.class,
                PmdRulesDefinition.class,
                PmdUnitTestsRulesDefinition.class,
                PmdProfileExporter.class,
                PmdProfileImporter.class,
                PmdViolationRecorder.class
        );
    }

    // TODO Compare expected classes with all classes annotated with ScannerSide annotation.
}

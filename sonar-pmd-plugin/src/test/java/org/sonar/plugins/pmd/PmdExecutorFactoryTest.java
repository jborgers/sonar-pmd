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
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.java.api.JavaResourceLocator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PmdExecutorFactoryTest {

    private final FileSystem fs = mock(FileSystem.class);
    private final ActiveRules activeRules = mock(ActiveRules.class);
    private final PmdConfiguration pmdConfiguration = mock(PmdConfiguration.class);
    private final Configuration settings = mock(Configuration.class);
    private final JavaResourceLocator javaResourceLocator = mock(JavaResourceLocator.class);

    @Test
    void should_create_executor_with_java_resource_locator_when_available() {
        // given
        PmdExecutorFactory factory = new PmdExecutorFactory(fs, activeRules, pmdConfiguration, settings, javaResourceLocator);

        // when
        PmdExecutor executor = factory.create();

        // then
        assertThat(executor).isNotNull();
    }

    @Test
    void should_create_executor_without_java_resource_locator_when_not_available() {
        // given
        PmdExecutorFactory factory = new PmdExecutorFactory(fs, activeRules, pmdConfiguration, settings);

        // when
        PmdExecutor executor = factory.create();

        // then
        assertThat(executor).isNotNull();
    }
}
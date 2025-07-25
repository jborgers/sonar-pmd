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

import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.reporting.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultInputFile;

import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

class PmdKotlinExecutorTest extends AbstractPmdExecutorTest {

    private PmdKotlinExecutor realPmdExecutor;

    @BeforeEach
    void setUp() {
        realPmdExecutor = new PmdKotlinExecutor(
                fileSystem,
                activeRules,
                pmdConfiguration,
                settings.asConfig()
        );
        pmdExecutor = Mockito.spy(realPmdExecutor);
    }

    @Override
    protected DefaultInputFile getAppropriateInputFileForTest() {
        return fileKotlin("src/test/kotlin/TestKotlin.kt", Type.MAIN);
    }

    @Test
    void should_execute_pmd_on_kotlin_source_files() {
        // Given
        DefaultInputFile srcFile = fileKotlin("src/test/kotlin/TestKotlin.kt", Type.MAIN);
        setupPmdRuleSet(PmdConstants.MAIN_KOTLIN_REPOSITORY_KEY, "simple-kotlin.xml");
        fileSystem.add(srcFile);

        // When
        Report report = pmdExecutor.execute();

        // Then
        assertThat(report).isNotNull();
        assertThat(report.getViolations()).hasSize(1);
        assertThat(report.getProcessingErrors()).isEmpty();
        verify(pmdConfiguration).dumpXmlReport(report);
    }

    @Test
    void should_execute_pmd_on_kotlin_test_files() {
        // Given
        DefaultInputFile testFile = fileKotlin("src/test/kotlin/TestKotlinTest.kt", Type.TEST);
        setupPmdRuleSet(PmdConstants.MAIN_KOTLIN_REPOSITORY_KEY, "simple-kotlin.xml");
        fileSystem.add(testFile);

        // When
        Report report = pmdExecutor.execute();

        // Then
        assertThat(report).isNotNull();
        verify(pmdConfiguration).dumpXmlReport(report);
    }

    @Test
    void should_ignore_empty_kotlin_test_dir() {
        // Given
        DefaultInputFile srcFile = fileKotlin("src/test/kotlin/TestKotlin.kt", Type.MAIN);
        doReturn(pmdTemplate).when(pmdExecutor).createPmdTemplate(any(URLClassLoader.class));
        setupPmdRuleSet(PmdConstants.MAIN_KOTLIN_REPOSITORY_KEY, "simple-kotlin.xml");
        fileSystem.add(srcFile);

        // When
        pmdExecutor.execute();

        // Then
        verify(pmdTemplate).process(anyIterable(), any(RuleSet.class));
        verifyNoMoreInteractions(pmdTemplate);
    }

    @Test
    void should_create_empty_classloader() throws Exception {
        // When
        pmdExecutor.execute();

        // Then
        // Verify that createPmdTemplate is called with a URLClassLoader that has no URLs
        verify(pmdExecutor).createPmdTemplate(argThat(classLoader -> 
                classLoader instanceof URLClassLoader && ((URLClassLoader) classLoader).getURLs().length == 0));
    }
}
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

import net.sourceforge.pmd.reporting.FileAnalysisListener;
import net.sourceforge.pmd.reporting.Report;
import net.sourceforge.pmd.reporting.RuleViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

import java.io.File;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PmdSensorTest {

    private final ActiveRules profile = mock(ActiveRules.class, RETURNS_DEEP_STUBS);
    private final PmdExecutor executor = mock(PmdExecutor.class);
    private final PmdViolationRecorder pmdViolationRecorder = mock(PmdViolationRecorder.class);
    private final SensorContext sensorContext = mock(SensorContext.class);
    private final DefaultFileSystem fs = new DefaultFileSystem(new File("."));

    private PmdSensor pmdSensor;

    @BeforeEach
    void setUpPmdSensor() {
        pmdSensor = new PmdSensor(profile, executor, pmdViolationRecorder, fs);
    }

    @Test
    void should_execute_on_project_without_main_files() {

        // given
        addOneJavaFile(Type.TEST);

        // when
        pmdSensor.execute(sensorContext);

        // then
        verify(executor, atLeastOnce()).execute();
    }

    @Test
    void should_execute_on_project_without_test_files() {

        // given
        addOneJavaFile(Type.MAIN);

        // when
        pmdSensor.execute(sensorContext);

        // then
        verify(executor, atLeastOnce()).execute();
    }

    @Test
    void should_not_execute_on_project_without_any_files() {

        // given
        // no files

        // when
        pmdSensor.execute(sensorContext);

        // then
        verify(executor, never()).execute();
    }

    @Test
    void should_not_execute_on_project_without_active_rules() {

        // given
        addOneJavaFile(Type.MAIN);
        addOneJavaFile(Type.TEST);

        when(profile.findByRepository(PmdConstants.MAIN_JAVA_REPOSITORY_KEY).isEmpty()).thenReturn(true);
        when(profile.findByRepository(PmdConstants.TEST_JAVA_REPOSITORY_KEY).isEmpty()).thenReturn(true);

        // when
        pmdSensor.execute(sensorContext);

        // then
        verify(executor, never()).execute();
    }

    @Test
    void should_report_violations() {

        // given
        addOneJavaFile(Type.MAIN);
        final RuleViolation pmdViolation = violation();
        mockExecutorResult(pmdViolation);

        // when
        pmdSensor.execute(sensorContext);

        // then
        verify(pmdViolationRecorder).saveViolation(pmdViolation, sensorContext);
    }

    @Test
    void should_not_report_zero_violation() {

        // given
        mockExecutorResult();

        // when
        pmdSensor.execute(sensorContext);

        // then
        verify(pmdViolationRecorder, never()).saveViolation(any(RuleViolation.class), eq(sensorContext));
        verifyNoMoreInteractions(sensorContext);
    }

    @Test
    void should_not_report_invalid_violation() {

        // given
        mockExecutorResult(violation());

        // when
        pmdSensor.execute(sensorContext);

        // then
        verify(pmdViolationRecorder, never()).saveViolation(any(RuleViolation.class), eq(sensorContext));
        verifyNoMoreInteractions(sensorContext);
    }

    @Test
    void pmdSensorShouldNotRethrowOtherExceptions() {

        // given
        addOneJavaFile(Type.MAIN);

        final RuntimeException expectedException = new RuntimeException();
        when(executor.execute()).thenThrow(expectedException);

        // when
        final Throwable thrown = catchThrowable(() -> pmdSensor.execute(sensorContext));

        // then
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .isEqualTo(expectedException);
    }

    @Test
    void should_to_string() {
        final String toString = pmdSensor.toString();
        assertThat(toString).isEqualTo("PmdSensor");
    }

    @Test
    void whenDescribeCalledThenSensorDescriptionIsWritten() {

        // given
        final SensorDescriptor mockDescriptor = mock(SensorDescriptor.class);
        when(mockDescriptor.onlyOnLanguages(anyString(), anyString())).thenReturn(mockDescriptor);

        // when
        pmdSensor.describe(mockDescriptor);

        // then
        verify(mockDescriptor).onlyOnLanguages(PmdConstants.LANGUAGE_JAVA_KEY, PmdConstants.LANGUAGE_KOTLIN_KEY);
        verify(mockDescriptor).name("PmdSensor");
    }

    private static RuleViolation violation() {
        return mock(RuleViolation.class);
    }

    private void mockExecutorResult(RuleViolation... violations) {

        Consumer<FileAnalysisListener> fileAnalysisListenerConsumer = fal -> {
            for (RuleViolation violation : violations) {
                fal.onRuleViolation(violation);
            }
        };

        final Report report = Report.buildReport(fileAnalysisListenerConsumer);

        when(executor.execute())
                .thenReturn(report);
    }

    private void addOneJavaFile(Type type) {
        mockExecutorResult();
        File file = new File("x");
        fs.add(
                TestInputFileBuilder.create(
                        "sonar-pmd-test",
                        file.getName()
                )
                        .setLanguage("java")
                        .setType(type)
                        .build()
        );
    }
}

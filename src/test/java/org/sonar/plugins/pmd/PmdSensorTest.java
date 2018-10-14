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

import java.io.File;

import com.google.common.collect.Iterators;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleViolation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class PmdSensorTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private final Project project = mock(Project.class);
    private final RulesProfile profile = mock(RulesProfile.class, RETURNS_DEEP_STUBS);
    private final PmdExecutor executor = mock(PmdExecutor.class);
    private final PmdViolationRecorder pmdViolationRecorder = mock(PmdViolationRecorder.class);
    private final SensorContext sensorContext = mock(SensorContext.class);
    private final DefaultFileSystem fs = new DefaultFileSystem(new File("."));

    private PmdSensor pmdSensor;

    private static RuleViolation violation() {
        return mock(RuleViolation.class);
    }

    private static Report report(RuleViolation... violations) {
        Report report = mock(Report.class);
        when(report.iterator()).thenReturn(Iterators.forArray(violations));
        return report;
    }

    @Before
    public void setUpPmdSensor() {
        pmdSensor = new PmdSensor(profile, executor, pmdViolationRecorder, fs);
    }

    @Test
    public void should_execute_on_project_without_main_files() {
        addOneJavaFile(Type.TEST);

        boolean shouldExecute = pmdSensor.shouldExecuteOnProject(project);

        assertThat(shouldExecute).isTrue();
    }

    @Test
    public void should_execute_on_project_without_test_files() {
        addOneJavaFile(Type.MAIN);

        boolean shouldExecute = pmdSensor.shouldExecuteOnProject(project);

        assertThat(shouldExecute).isTrue();
    }

    @Test
    public void should_not_execute_on_project_without_any_files() {
        boolean shouldExecute = pmdSensor.shouldExecuteOnProject(project);

        assertThat(shouldExecute).isFalse();
    }

    @Test
    public void should_not_execute_on_project_without_active_rules() {
        addOneJavaFile(Type.MAIN);
        addOneJavaFile(Type.TEST);

        when(profile.getActiveRulesByRepository(PmdConstants.REPOSITORY_KEY).isEmpty()).thenReturn(true);
        when(profile.getActiveRulesByRepository(PmdConstants.TEST_REPOSITORY_KEY).isEmpty()).thenReturn(true);

        boolean shouldExecute = pmdSensor.shouldExecuteOnProject(project);

        assertThat(shouldExecute).isFalse();
    }

    @Test
    public void should_report_violations() {
        RuleViolation pmdViolation = violation();
        Report report = report(pmdViolation);
        when(executor.execute()).thenReturn(report);

        pmdSensor.analyse(project, sensorContext);

        verify(pmdViolationRecorder).saveViolation(pmdViolation);
    }

    @Test
    public void shouldnt_report_zero_violation() {
        Report report = report();
        when(executor.execute()).thenReturn(report);

        pmdSensor.analyse(project, sensorContext);

        verifyZeroInteractions(sensorContext);
    }

    @Test
    public void should_not_report_invalid_violation() {
        RuleViolation pmdViolation = violation();
        Report report = report(pmdViolation);
        when(executor.execute()).thenReturn(report);
        when(report.iterator()).thenReturn(Iterators.forArray(pmdViolation));

        pmdSensor.analyse(project, sensorContext);

        verifyZeroInteractions(sensorContext);
    }

    @Test
    public void pmdSensorShouldNotRethrowOtherExceptions() {
        final RuntimeException expectedException = new RuntimeException();
        when(executor.execute()).thenThrow(expectedException);

        exception.expect(RuntimeException.class);
        exception.expect(equalTo(expectedException));

        pmdSensor.analyse(project, sensorContext);
    }

    @Test
    public void should_to_string() {
        String toString = pmdSensor.toString();

        assertThat(toString).isEqualTo("PmdSensor");
    }

    private void addOneJavaFile(Type type) {
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

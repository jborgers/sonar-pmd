/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2019 SonarSource SA
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

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleViolation;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PmdViolationRecorderTest {

    private final DefaultFileSystem spiedFs = spy(new DefaultFileSystem(new File("").getAbsoluteFile()));
    private final ActiveRules mockActiveRules = mock(ActiveRules.class);
    private final SensorContext mockContext = mock(SensorContext.class);

    private final PmdViolationRecorder pmdViolationRecorder = new PmdViolationRecorder(spiedFs, mockActiveRules);

    @Test
    void should_convert_pmd_violation_to_sonar_violation() {

        // given
        final ActiveRule rule = createRuleInActiveRules();
        final File file1 = new File("src/source.java");
        final DefaultInputFile inputFile1 = addToFileSystem(file1);
        final RuleViolation pmdViolation = createPmdViolation(file1, "RULE");
        final NewIssue newIssue = mock(NewIssue.class);
        final NewIssueLocation issueLocation = mock(NewIssueLocation.class);

        when(mockContext.newIssue()).thenReturn(newIssue);
        when(newIssue.forRule(rule.ruleKey())).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(issueLocation);
        when(newIssue.at(issueLocation)).thenReturn(newIssue);
        when(issueLocation.on(inputFile1)).thenReturn(issueLocation);
        when(issueLocation.message("Description")).thenReturn(issueLocation);
        when(issueLocation.at(any(TextRange.class))).thenReturn(issueLocation);

        // when
        pmdViolationRecorder.saveViolation(pmdViolation, mockContext);

        // then
        verify(mockContext).newIssue();
        verify(newIssue).save();
    }

    @Test
    void should_ignore_violation_on_unknown_resource() {

        // given
        final File unknownFile = new File("src/UNKNOWN.java");
        final RuleViolation pmdViolation = createPmdViolation(unknownFile, "RULE");

        // when
        pmdViolationRecorder.saveViolation(pmdViolation, mockContext);

        // then
        verifyNoMoreInteractions(mockActiveRules);
        verifyNoMoreInteractions(mockContext);
        verify(spiedFs).inputFile(any(FilePredicate.class));
    }

    @Test
    void should_ignore_violation_on_unknown_rule() {

        // given
        final File file1 = new File("test/source.java");
        addToFileSystem(file1);
        final String ruleName = "UNKNOWN";
        final RuleViolation pmdViolation = createPmdViolation(file1, ruleName);
        final RuleKey expectedRuleKey1 = RuleKey.of(PmdConstants.REPOSITORY_KEY, ruleName);
        final RuleKey expectedRuleKey2 = RuleKey.of(PmdConstants.REPOSITORY_KEY, ruleName);

        // when
        pmdViolationRecorder.saveViolation(pmdViolation, mockContext);

        // then
        verify(spiedFs).inputFile(any(FilePredicate.class));
        verify(mockActiveRules).find(expectedRuleKey1);
        verify(mockActiveRules).find(expectedRuleKey2);
        verifyNoMoreInteractions(mockContext);
    }

    private DefaultInputFile addToFileSystem(File file) {
        DefaultInputFile inputFile = TestInputFileBuilder
                .create("test", spiedFs.baseDir(), file.getAbsoluteFile())
                .setContents("This\nis\na text\nfile.")
                .build();
        spiedFs.add(inputFile);
        return inputFile;
    }

    private ActiveRule createRuleInActiveRules() {
        ActiveRule sonarRule = mock(ActiveRule.class);
        RuleKey ruleKey = RuleKey.of("pmd", "RULE");
        when(mockActiveRules.find(ruleKey)).thenReturn(sonarRule);
        when(sonarRule.ruleKey()).thenReturn(RuleKey.of("pmd", "RULE"));
        return sonarRule;
    }

    private RuleViolation createPmdViolation(File file, String ruleName) {
        final Rule rule = mock(Rule.class);
        final RuleViolation pmdViolation = mock(RuleViolation.class);

        when(rule.getName()).thenReturn(ruleName);
        when(pmdViolation.getFilename()).thenReturn(file.toURI().toString());
        when(pmdViolation.getBeginLine()).thenReturn(2);
        when(pmdViolation.getDescription()).thenReturn("Description");
        when(pmdViolation.getRule()).thenReturn(rule);

        return pmdViolation;
    }
}

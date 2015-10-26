/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.pmd;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleViolation;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleFinder;

import java.io.File;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class PmdViolationRecorderTest {

  private DefaultFileSystem fs = new DefaultFileSystem(new File("."));
  private RuleFinder ruleFinder = mock(RuleFinder.class);
  private ResourcePerspectives perspectives = mock(ResourcePerspectives.class);
  private PmdViolationRecorder pmdViolationRecorder =
    new PmdViolationRecorder(fs, ruleFinder, perspectives);

  @Test
  public void should_convert_pmd_violation_to_sonar_violation() {
    org.sonar.api.rules.Rule sonarRule = createRuleInRuleFinder("RULE");
    File file1 = new File("src/source.java");
    DefaultInputFile inputFile1 = addToFileSystem(file1);
    Issuable issuable = createIssuable(inputFile1);
    RuleViolation pmdViolation = createPmdViolation(file1, 42, "Description", "RULE");

    Issue issue = mock(Issue.class);
    IssueBuilder issueBuilder = mock(IssueBuilder.class);
    when(issuable.newIssueBuilder()).thenReturn(issueBuilder);
    when(issueBuilder.ruleKey(sonarRule.ruleKey())).thenReturn(issueBuilder);
    when(issueBuilder.message("Description")).thenReturn(issueBuilder);
    when(issueBuilder.line(42)).thenReturn(issueBuilder);
    when(issueBuilder.build()).thenReturn(issue);

    pmdViolationRecorder.saveViolation(pmdViolation);
    verify(issuable).addIssue(issue);
    verify(issueBuilder).ruleKey(sonarRule.ruleKey());
    verify(issueBuilder).message("Description");
    verify(issueBuilder).line(42);
    verify(sonarRule, atLeastOnce()).ruleKey();
  }

  @Test
  public void should_ignore_violation_on_unknown_resource() {
    org.sonar.api.rules.Rule sonarRule = createRuleInRuleFinder("RULE");
    File unknownFile = new File("src/UNKNOWN.java");
    RuleViolation pmdViolation = createPmdViolation(unknownFile, 42, "Description", "RULE");

    pmdViolationRecorder.saveViolation(pmdViolation);
    verifyZeroInteractions(sonarRule);
  }

  @Test
  public void should_ignore_violation_on_non_issuable_resource() {
    org.sonar.api.rules.Rule sonarRule = createRuleInRuleFinder("RULE");
    File file1 = new File("test/source.java");
    addToFileSystem(file1);
    RuleViolation pmdViolation = createPmdViolation(file1, 42, "Description", "RULE");

    pmdViolationRecorder.saveViolation(pmdViolation);
    verifyZeroInteractions(sonarRule);
  }

  @Test
  public void should_ignore_violation_on_unknown_rule() {
    File file1 = new File("test/source.java");
    DefaultInputFile inputFile1 = addToFileSystem(file1);
    Issuable issuable = createIssuable(inputFile1);
    RuleViolation pmdViolation = createPmdViolation(file1, 42, "Description", "UNKNOWN");

    pmdViolationRecorder.saveViolation(pmdViolation);
    verifyZeroInteractions(issuable);
  }

  private DefaultInputFile addToFileSystem(File file) {
    DefaultInputFile inputFile = new DefaultInputFile(file.getPath()).setAbsolutePath(file.getAbsolutePath());
    fs.add(inputFile);
    return inputFile;
  }

  private Issuable createIssuable(InputFile file) {
    Issuable issuable = mock(Issuable.class);
    when(perspectives.as(Issuable.class, file)).thenReturn(issuable);
    return issuable;
  }

  private org.sonar.api.rules.Rule createRuleInRuleFinder(String ruleName) {
    org.sonar.api.rules.Rule sonarRule = mock(org.sonar.api.rules.Rule.class);
    when(ruleFinder.findByKey("pmd", ruleName)).thenReturn(sonarRule);
    when(sonarRule.ruleKey()).thenReturn(RuleKey.of("pmd", ruleName));
    return sonarRule;
  }

  private RuleViolation createPmdViolation(File file, int line, String description, String ruleName) {
    Rule rule = mock(Rule.class);
    when(rule.getName()).thenReturn(ruleName);
    RuleViolation pmdViolation = mock(RuleViolation.class);
    when(pmdViolation.getFilename()).thenReturn(file.getAbsolutePath());
    when(pmdViolation.getBeginLine()).thenReturn(line);
    when(pmdViolation.getDescription()).thenReturn(description);
    when(pmdViolation.getRule()).thenReturn(rule);
    return pmdViolation;
  }
}

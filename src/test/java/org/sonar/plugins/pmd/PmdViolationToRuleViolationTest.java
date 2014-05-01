/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
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

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.Rule;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;

import java.io.File;
import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.test.MoreConditions.reflectionEqualTo;

public class PmdViolationToRuleViolationTest {
  private org.sonar.api.rules.Rule sonarRule = org.sonar.api.rules.Rule.create("pmd", "RULE");
  private ProjectFileSystem projectFileSystem = mock(ProjectFileSystem.class);
  private Project project = when(mock(Project.class).getFileSystem()).thenReturn(projectFileSystem).getMock();
  private RuleViolation pmdViolation = mock(RuleViolation.class);
  private SensorContext context = mock(SensorContext.class);
  private RuleFinder ruleFinder = mock(RuleFinder.class);
  private Rule rule = mock(Rule.class);

  @Test
  public void should_convert_pmd_violation_to_sonar_violation() {
    when(projectFileSystem.getSourceDirs()).thenReturn(Arrays.asList(new File("/src")));
    when(pmdViolation.getFilename()).thenReturn("/src/source.java");
    when(pmdViolation.getBeginLine()).thenReturn(42);
    when(pmdViolation.getDescription()).thenReturn("Description");
    when(pmdViolation.getRule()).thenReturn(rule);
    when(rule.getName()).thenReturn("RULE");
    when(context.getResource(new org.sonar.api.resources.File("source.java"))).thenReturn(new org.sonar.api.resources.File("source.java"));
    when(ruleFinder.findByKey("pmd", "RULE")).thenReturn(sonarRule);

    PmdViolationToRuleViolation pmdViolationToRuleViolation = new PmdViolationToRuleViolation(project, ruleFinder);
    Violation violation = pmdViolationToRuleViolation.toViolation(pmdViolation, context);

    assertThat(violation).is(reflectionEqualTo(Violation.create(sonarRule, new org.sonar.api.resources.File("source.java")).setLineId(42).setMessage("Description")));
  }

  @Test
  public void should_ignore_violation_on_unknown_resource() {
    when(projectFileSystem.getSourceDirs()).thenReturn(Arrays.asList(new File("/src")));
    when(pmdViolation.getFilename()).thenReturn("/src/UNKNOWN.java");

    PmdViolationToRuleViolation pmdViolationToRuleViolation = new PmdViolationToRuleViolation(project, ruleFinder);
    Violation violation = pmdViolationToRuleViolation.toViolation(pmdViolation, context);

    assertThat(violation).isNull();
  }

  @Test
  public void should_ignore_violation_on_unknown_rule() {
    when(projectFileSystem.getTestDirs()).thenReturn(Arrays.asList(new File("/test")));
    when(pmdViolation.getFilename()).thenReturn("/test/source.java");
    when(pmdViolation.getRule()).thenReturn(rule);
    when(rule.getName()).thenReturn("UNKNOWN");
    when(context.getResource(new org.sonar.api.resources.File("source.java"))).thenReturn(new org.sonar.api.resources.File("source.java"));

    PmdViolationToRuleViolation pmdViolationToRuleViolation = new PmdViolationToRuleViolation(project, ruleFinder);
    Violation violation = pmdViolationToRuleViolation.toViolation(pmdViolation, context);

    assertThat(violation).isNull();
  }
}

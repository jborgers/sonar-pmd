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

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleViolation;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    when(projectFileSystem.getBasedir()).thenReturn(new File("src"));
    String absolutePath = new File("src/source.java").getAbsolutePath();
    when(pmdViolation.getFilename()).thenReturn(absolutePath);
    when(pmdViolation.getBeginLine()).thenReturn(42);
    when(pmdViolation.getDescription()).thenReturn("Description");
    when(pmdViolation.getRule()).thenReturn(rule);
    when(rule.getName()).thenReturn("RULE");
    org.sonar.api.resources.File file = org.sonar.api.resources.File.fromIOFile(new File(absolutePath), project);
    when(context.getResource(file)).thenReturn(file);
    when(ruleFinder.findByKey("pmd", "RULE")).thenReturn(sonarRule);

    PmdViolationToRuleViolation pmdViolationToRuleViolation = new PmdViolationToRuleViolation(project, ruleFinder);
    Violation violation = pmdViolationToRuleViolation.toViolation(pmdViolation, context);
    assertThat(violation).isNotNull();
    assertThat(violation.getRule()).isEqualTo(sonarRule);
    assertThat(violation.getResource()).isEqualTo(file);
    assertThat(violation.getLineId()).isEqualTo(42);
    assertThat(violation.getMessage()).isEqualTo("Description");
  }

  @Test
  public void should_ignore_violation_on_unknown_resource() {
    when(projectFileSystem.getBasedir()).thenReturn(new File("src"));
    when(pmdViolation.getFilename()).thenReturn(new File("src/UNKNOWN.java").getAbsolutePath());

    PmdViolationToRuleViolation pmdViolationToRuleViolation = new PmdViolationToRuleViolation(project, ruleFinder);
    Violation violation = pmdViolationToRuleViolation.toViolation(pmdViolation, context);

    assertThat(violation).isNull();
  }

  @Test
  public void should_ignore_violation_on_unknown_rule() {
    when(projectFileSystem.getBasedir()).thenReturn(new File(""));
    String absolutePath = new File("test/source.java").getAbsolutePath();
    when(pmdViolation.getFilename()).thenReturn(absolutePath);
    when(pmdViolation.getRule()).thenReturn(rule);
    when(rule.getName()).thenReturn("UNKNOWN");
    org.sonar.api.resources.File file = org.sonar.api.resources.File.fromIOFile(new File(absolutePath), project);
    when(context.getResource(file)).thenReturn(file);

    PmdViolationToRuleViolation pmdViolationToRuleViolation = new PmdViolationToRuleViolation(project, ruleFinder);
    Violation violation = pmdViolationToRuleViolation.toViolation(pmdViolation, context);

    assertThat(violation).isNull();
  }
}

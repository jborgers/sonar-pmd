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
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;

public class PmdViolationToRuleViolation implements BatchExtension {
  private final Project project;
  private final RuleFinder ruleFinder;

  public PmdViolationToRuleViolation(Project project, RuleFinder ruleFinder) {
    this.project = project;
    this.ruleFinder = ruleFinder;
  }

  public Violation toViolation(RuleViolation pmdViolation, SensorContext context) {
    Resource resource = findResourceFor(pmdViolation);
    if (context.getResource(resource) == null) {
      // Save violations only for existing resources
      return null;
    }

    Rule rule = findRuleFor(pmdViolation);
    if (rule == null) {
      // Save violations only for enabled rules
      return null;
    }

    int lineId = pmdViolation.getBeginLine();
    String message = pmdViolation.getDescription();

    return Violation.create(rule, resource).setLineId(lineId).setMessage(message);
  }

  private Resource findResourceFor(RuleViolation violation) {
    return File.fromIOFile(new java.io.File(violation.getFilename()), project);
  }

  private Rule findRuleFor(RuleViolation violation) {
    String ruleKey = violation.getRule().getName();
    Rule rule = ruleFinder.findByKey(PmdConstants.REPOSITORY_KEY, ruleKey);
    if (rule != null) {
      return rule;
    }
    return ruleFinder.findByKey(PmdConstants.TEST_REPOSITORY_KEY, ruleKey);
  }

}

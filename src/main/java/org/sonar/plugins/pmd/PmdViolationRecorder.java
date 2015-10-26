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

import net.sourceforge.pmd.RuleViolation;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;

public class PmdViolationRecorder implements BatchExtension {

  private final FileSystem fs;
  private final RuleFinder ruleFinder;
  private final ResourcePerspectives perspectives;

  public PmdViolationRecorder(FileSystem fs, RuleFinder ruleFinder, ResourcePerspectives perspectives) {
    this.fs = fs;
    this.ruleFinder = ruleFinder;
    this.perspectives = perspectives;
  }

  public void saveViolation(RuleViolation pmdViolation) {
    InputFile inputFile = findResourceFor(pmdViolation);
    if (inputFile == null) {
      // Save violations only for existing resources
      return;
    }

    Issuable issuable = perspectives.as(Issuable.class, inputFile);

    Rule rule = findRuleFor(pmdViolation);
    if (issuable == null || rule == null) {
      // Save violations only for enabled rules
      return;
    }

    IssueBuilder issueBuilder = issuable.newIssueBuilder()
      .ruleKey(rule.ruleKey())
      .message(pmdViolation.getDescription())
      .line(pmdViolation.getBeginLine());
    issuable.addIssue(issueBuilder.build());
  }

  private InputFile findResourceFor(RuleViolation violation) {
    return fs.inputFile(fs.predicates().hasAbsolutePath(violation.getFilename()));
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

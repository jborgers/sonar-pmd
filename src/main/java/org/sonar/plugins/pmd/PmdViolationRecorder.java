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

import java.net.URI;

import net.sourceforge.pmd.RuleViolation;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

@ScannerSide
public class PmdViolationRecorder {

    private final FileSystem fs;
    private final ActiveRules activeRules;

    public PmdViolationRecorder(FileSystem fs, ActiveRules activeRules) {
        this.fs = fs;
        this.activeRules = activeRules;
    }

    public void saveViolation(RuleViolation pmdViolation, SensorContext context) {
        final InputFile inputFile = findResourceFor(pmdViolation);
        if (inputFile == null) {
            // Save violations only for existing resources
            return;
        }

        final RuleKey ruleKey = findActiveRuleKeyFor(pmdViolation);

        if (ruleKey == null) {
            // Save violations only for enabled rules
            return;
        }

        final NewIssue issue = context.newIssue()
                .forRule(ruleKey);

        final TextRange issueTextRange = issueRangeFor(pmdViolation, inputFile);

        final NewIssueLocation issueLocation = issue.newLocation()
                .on(inputFile)
                .message(pmdViolation.getDescription())
                .at(issueTextRange);

        issue.at(issueLocation)
                .save();
    }

    private TextRange issueRangeFor(RuleViolation pmdViolation, InputFile inputFile) {

        final int startLine = pmdViolation.getBeginLine();
        final int endLine = pmdViolation.getEndLine() > 0 ? pmdViolation.getEndLine() : pmdViolation.getBeginLine();

        // PMD counts TABs differently, so we can not use RuleViolation#getBeginColumn and RuleViolation#getEndColumn
        // Therefore, we select complete lines.
        final TextPointer startPointer = inputFile.selectLine(startLine).start();
        final TextPointer endPointer = inputFile.selectLine(endLine).end();

        return inputFile.newRange(startPointer, endPointer);
    }

    private InputFile findResourceFor(RuleViolation violation) {
        final URI uri = URI.create(violation.getFilename());
        return fs.inputFile(
                fs.predicates().hasURI(uri)
        );
    }

    private RuleKey findActiveRuleKeyFor(RuleViolation violation) {
        final String internalRuleKey = violation.getRule().getName();
        RuleKey ruleKey = RuleKey.of(PmdConstants.REPOSITORY_KEY, internalRuleKey);

        if (activeRules.find(ruleKey) != null) {
            return ruleKey;
        }

        // Let's try the test repo.
        ruleKey = RuleKey.of(PmdConstants.TEST_REPOSITORY_KEY, internalRuleKey);

        return activeRules.find(ruleKey) != null ? ruleKey : null;
    }
}

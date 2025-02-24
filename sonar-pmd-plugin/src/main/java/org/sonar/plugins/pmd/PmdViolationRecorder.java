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

import net.sourceforge.pmd.reporting.RuleViolation;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.pmd.rule.PmdKotlinRulesDefinition;

import java.util.Optional;

@ScannerSide
public class PmdViolationRecorder {

    private static final Logger LOGGER = Loggers.get(PmdKotlinRulesDefinition.class);

    private final FileSystem fs;
    private final ActiveRules activeRules;

    public PmdViolationRecorder(FileSystem fs, ActiveRules activeRules) {
        this.fs = fs;
        this.activeRules = activeRules;
    }

    public void saveViolation(RuleViolation pmdViolation, SensorContext context) {

        LOGGER.debug("About to save RuleViolation: {}", pmdViolation);

        final InputFile inputFile = findResourceFor(pmdViolation);

        LOGGER.trace("Found violation input file: {}", inputFile);

        if (inputFile == null) {
            // Save violations only for existing resources
            return;
        }

        final RuleKey ruleKey = findActiveRuleKeyFor(pmdViolation);

        LOGGER.trace("Found violation rule key: {}", ruleKey);

        if (ruleKey == null) {
            // Save violations only for enabled rules
            return;
        }

        final NewIssue issue = context.newIssue()
                .forRule(ruleKey);

        final TextRange issueTextRange = TextRangeCalculator.calculate(pmdViolation, inputFile);

        LOGGER.trace("New issue: {} Text range: {}", issue, issueTextRange);

        final NewIssueLocation issueLocation = issue.newLocation()
                .on(inputFile)
                .message(pmdViolation.getDescription())
                .at(issueTextRange);

        LOGGER.trace("Issue location to save: {}", issueLocation);

        issue.at(issueLocation)
                .save();

        LOGGER.debug("RuleViolation saved: {}", pmdViolation);
    }

    private InputFile findResourceFor(RuleViolation violation) {
        return fs.inputFile(
                fs.predicates().hasAbsolutePath(
                        violation.getFileId().getAbsolutePath()
                )
        );
    }

    private RuleKey findActiveRuleKeyFor(RuleViolation violation) {
        final String internalRuleKey = violation.getRule().getName();

        return findRuleKey(internalRuleKey, PmdConstants.MAIN_JAVA_REPOSITORY_KEY)
            .orElse(findRuleKey(internalRuleKey, PmdConstants.TEST_JAVA_REPOSITORY_KEY)
                .orElse(findRuleKey(internalRuleKey, PmdConstants.MAIN_KOTLIN_REPOSITORY_KEY)
                    .orElse(null)));
    }

    private Optional<RuleKey> findRuleKey(String internalRuleKey, String repositoryKey) {
        RuleKey ruleKey = RuleKey.of(repositoryKey, internalRuleKey);
        if (activeRules.find(ruleKey) != null) {
            return Optional.of(ruleKey);
        }
        return Optional.empty();
    }
}

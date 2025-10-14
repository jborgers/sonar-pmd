/*
 * Shared violation recorder moved to lib to be reused by plugins.
 */
package org.sonar.plugins.pmd;

import net.sourceforge.pmd.reporting.RuleViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import java.util.Optional;

@ScannerSide
public class PmdViolationRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PmdViolationRecorder.class);

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
            return;
        }

        final RuleKey ruleKey = findActiveRuleKeyFor(pmdViolation);

        LOGGER.trace("Found violation rule key: {}", ruleKey);

        if (ruleKey == null) {
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
                .orElse(findRuleKey(internalRuleKey, PmdConstants.MAIN_KOTLIN_REPOSITORY_KEY)
                    .orElse(findRuleKey(internalRuleKey, PmdConstants.MAIN_APEX_REPOSITORY_KEY)
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

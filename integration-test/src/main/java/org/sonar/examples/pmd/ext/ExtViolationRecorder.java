package org.sonar.examples.pmd.ext;

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

@ScannerSide
public class ExtViolationRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtViolationRecorder.class);

    private final FileSystem fs;
    private final ActiveRules activeRules;

    public ExtViolationRecorder(FileSystem fs, ActiveRules activeRules) {
        this.fs = fs;
        this.activeRules = activeRules;
    }

    public void saveViolation(RuleViolation pmdViolation, SensorContext context) {
        final InputFile inputFile = fs.inputFile(
                fs.predicates().hasAbsolutePath(
                        pmdViolation.getFileId().getAbsolutePath()
                )
        );
        if (inputFile == null) {
            return;
        }

        final RuleKey ruleKey = RuleKey.of(ExtConstants.REPOSITORY_KEY, pmdViolation.getRule().getName());
        if (activeRules.find(ruleKey) == null) {
            return;
        }

        final NewIssue issue = context.newIssue().forRule(ruleKey);
        final TextRange issueTextRange = TextRangeCalculator.calculate(pmdViolation, inputFile);
        final NewIssueLocation issueLocation = issue.newLocation().on(inputFile).message(pmdViolation.getDescription()).at(issueTextRange);
        issue.at(issueLocation).save();
    }
}

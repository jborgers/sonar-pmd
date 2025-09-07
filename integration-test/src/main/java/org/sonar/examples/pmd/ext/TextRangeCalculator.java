package org.sonar.examples.pmd.ext;

import net.sourceforge.pmd.reporting.RuleViolation;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;

class TextRangeCalculator {

    private TextRangeCalculator() {
    }

    static TextRange calculate(RuleViolation pmdViolation, InputFile inputFile) {
        final int startLine = calculateBeginLine(pmdViolation);
        final int endLine = calculateEndLine(pmdViolation);
        final TextPointer startPointer = inputFile.selectLine(startLine).start();
        final TextPointer endPointer = inputFile.selectLine(endLine).end();
        return inputFile.newRange(startPointer, endPointer);
    }

    private static int calculateEndLine(RuleViolation pmdViolation) {
        return Math.max(pmdViolation.getBeginLine(), pmdViolation.getEndLine());
    }

    private static int calculateBeginLine(RuleViolation pmdViolation) {
        int minLine = Math.min(pmdViolation.getBeginLine(), pmdViolation.getEndLine());
        return minLine > 0 ? minLine : calculateEndLine(pmdViolation);
    }
}

/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2021 SonarSource SA and others
 * mailto:jens AT gerdes DOT digital
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

import net.sourceforge.pmd.RuleViolation;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TextRangeCalculatorTest {

    private final InputFile testInput = createTestInput();

    @Test
    void testCalculateTextRange() {

        // given
        final int beginLine = 1;
        final int endLine = 3;
        final RuleViolation mockedViolation = mock(RuleViolation.class);

        // when
        when(mockedViolation.getBeginLine()).thenReturn(beginLine);
        when(mockedViolation.getEndLine()).thenReturn(endLine);

        final TextRange result = TextRangeCalculator.calculate(mockedViolation, testInput);

        // then
        assertThat(result).isNotNull();
        assertThat(result.start())
                .isNotNull()
                .hasFieldOrPropertyWithValue("line", beginLine);

        assertThat(result.end())
                .isNotNull()
                .hasFieldOrPropertyWithValue("line", endLine);
    }

    @Test
    void whenEndLineIsGreaterThanBeginLineThenLineNumbersAreFlipped() {

        // given
        final int beginLine = 3;
        final int endLine = 1;
        final RuleViolation mockedViolation = mock(RuleViolation.class);

        // when
        when(mockedViolation.getBeginLine()).thenReturn(beginLine);
        when(mockedViolation.getEndLine()).thenReturn(endLine);

        final TextRange result = TextRangeCalculator.calculate(mockedViolation, testInput);

        // then
        assertThat(result).isNotNull();
        assertThat(result.start())
                .isNotNull()
                .hasFieldOrPropertyWithValue("line", endLine);

        assertThat(result.end())
                .isNotNull()
                .hasFieldOrPropertyWithValue("line", beginLine);
    }

    @Test
    void whenEndLineEqualsBeginLineThenRangeStartAndEndsAtSameLine() {
        // given
        final int line = 1;
        final RuleViolation mockedViolation = mock(RuleViolation.class);

        // when
        when(mockedViolation.getBeginLine()).thenReturn(line);
        when(mockedViolation.getEndLine()).thenReturn(line);

        final TextRange result = TextRangeCalculator.calculate(mockedViolation, testInput);

        // then
        assertThat(result).isNotNull();
        assertThat(result.start())
                .isNotNull()
                .hasFieldOrPropertyWithValue("line", line);

        assertThat(result.end())
                .isNotNull()
                .hasFieldOrPropertyWithValue("line", line);
    }

    @Test
    void whenBeginLineIsNegativeThenRangeStartsAndEndsAtEndLine() {

        // given
        final int beginLine = -2;
        final int endLine = 1;
        final RuleViolation mockedViolation = mock(RuleViolation.class);

        // when
        when(mockedViolation.getBeginLine()).thenReturn(beginLine);
        when(mockedViolation.getEndLine()).thenReturn(endLine);

        final TextRange result = TextRangeCalculator.calculate(mockedViolation, testInput);

        // then
        assertThat(result).isNotNull();
        assertThat(result.start())
                .isNotNull()
                .hasFieldOrPropertyWithValue("line", endLine);

        assertThat(result.end())
                .isNotNull()
                .hasFieldOrPropertyWithValue("line", endLine);
    }

    @Test
    void whenEndLineIsNegativeThenRangeStartsAndEndsAtBeginLine() {

        // given
        final int beginLine = 2;
        final int endLine = -1;
        final RuleViolation mockedViolation = mock(RuleViolation.class);

        // when
        when(mockedViolation.getBeginLine()).thenReturn(beginLine);
        when(mockedViolation.getEndLine()).thenReturn(endLine);

        final TextRange result = TextRangeCalculator.calculate(mockedViolation, testInput);

        // then
        assertThat(result).isNotNull();
        assertThat(result.start())
                .isNotNull()
                .hasFieldOrPropertyWithValue("line", beginLine);

        assertThat(result.end())
                .isNotNull()
                .hasFieldOrPropertyWithValue("line", beginLine);
    }

    private InputFile createTestInput() {
        return TestInputFileBuilder
                .create("moduleKey", "relPath")
                .setContents(
                        "This\n" +
                                "is a \n" +
                                "multi-line" +
                                "file."
                        )
                .build();
    }

}
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
package org.sonar.plugins.pmd.rule;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownToHtmlConverterTest {

    @Test
    void camelCaseToReadable_preserves_NaN() {
        assertThat(MarkdownToHtmlConverter.camelCaseToReadable("ComparisonWithNaN"))
                .isEqualTo("Comparison with NaN");
        assertThat(MarkdownToHtmlConverter.camelCaseToReadable("isNaN"))
                .isEqualTo("Is NaN");
    }

    @Test
    void testBackticksConvertToPreBlocks() {
        String input = "By default, the comment must be `/* default */` or `/* package */`,";
        String result = MarkdownToHtmlConverter.convertToHtml(input);

        assertThat(result)
                .contains("<code>/* default */</code>")
                .contains("<code>/* package */</code>")
                .doesNotContain("<code>/<i> default </i>/</code>")
                .doesNotContain("<code>/<i> package </i>/</code>");
    }

    @Test
    void testPlaceholderVariablesInBackticks() {
        // Simulate the scenario where placeholder processing has already converted {0} to <code>{0}</code>
        // and then backticks are processed
        String messageWithPlaceholders = "Do not use `new <code>{0}</code>(...)`, prefer `<code>{0}</code>.valueOf(...)`";
        String result = MarkdownToHtmlConverter.convertToHtml(messageWithPlaceholders);

        assertThat(result)
                .contains("<code>new {0}(...)</code>")
                .contains("<code>{0}.valueOf(...)</code>")
                .doesNotContain("<code><code>{0}</code></code>") // No nested code tags
                .doesNotContain("</p><p>"); // Should be in single paragraph
    }

    @Test
    void testBackticksDoNotProcessInsideExistingCodeBlocks() {
        String input = "Use <code>some `backticks` here</code> and also `standalone backticks`";
        String result = MarkdownToHtmlConverter.convertToHtml(input);

        assertThat(result)
                .contains("<code>some `backticks` here</code>") // Backticks preserved inside existing code
                .contains("<code>standalone backticks</code>"); // Standalone backticks converted
    }

    @Test
    void testBackticksDoNotProcessInsidePreBlocks() {
        String input = "Here is <code>some `backticks` in pre</code> and `standalone`";
        String result = MarkdownToHtmlConverter.convertToHtml(input);

        assertThat(result)
                .contains("<code>some `backticks` in pre</code>") // Backticks preserved inside pre
                .contains("<code>standalone</code>"); // Standalone backticks converted
    }

    @Test
    void testBackticksWithSpecialCharacters() {
        String input = "Use `String.valueOf(\"test\")` method";
        String result = MarkdownToHtmlConverter.convertToHtml(input);

        assertThat(result)
                .contains("<code>String.valueOf(&quot;test&quot;)</code>");
    }

    @Test
    void testMultipleBackticksInSameParagraph() {
        String input = "Compare `methodA()` with `methodB()` for differences";
        String result = MarkdownToHtmlConverter.convertToHtml(input);

        assertThat(result)
                .contains("<code>methodA()</code>")
                .contains("<code>methodB()</code>");
    }

    @Test
    void testEmptyBackticks() {
        String input = "Empty backticks `` should be handled";
        String result = MarkdownToHtmlConverter.convertToHtml(input);

        assertThat(result)
                .contains("<code></code>");
    }

    @Test
    void testBackticksWithMarkdownFormatting() {
        String input = "The `*bold*` and `_italic_` should not be processed inside backticks";
        String result = MarkdownToHtmlConverter.convertToHtml(input);

        assertThat(result)
                .contains("<code>*bold*</code>")
                .contains("<code>_italic_</code>")
                .doesNotContain("<b>bold</b>")
                .doesNotContain("<i>italic</i>");
    }
}
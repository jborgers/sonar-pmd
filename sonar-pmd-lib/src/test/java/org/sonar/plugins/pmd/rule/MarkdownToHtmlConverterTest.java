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
    void should_convert_empty_markdown_to_empty_string() {
        // given
        String markdown = "";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).isEmpty();
    }

    @Test
    void should_convert_null_markdown_to_empty_string() {
        // when
        String html = MarkdownToHtmlConverter.convertToHtml(null);

        // then
        assertThat(html).isEmpty();
    }

    @Test
    void should_convert_simple_paragraph() {
        // given
        String markdown = "This is a simple paragraph.";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).isEqualTo("<p>This is a simple paragraph.</p>");
    }

    @Test
    void should_convert_headers() {
        // given
        String markdown = "# Header 1\n## Header 2\n### Header 3";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).contains("<h1>Header 1</h1>");
        assertThat(html).contains("<h2>Header 2</h2>");
        assertThat(html).contains("<h3>Header 3</h3>");
    }

    @Test
    void should_convert_code_blocks() {
        // given
        String markdown = "Here is some `inline code` in a paragraph.";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).isEqualTo("<p>Here is some <code>inline code</code> in a paragraph.</p>");
    }

    @Test
    void should_convert_multi_line_code_blocks() {
        // given
        String markdown = "```java\npublic class Test {\n    // code\n}\n```";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).contains("<pre><code class=\"language-java\">");
        assertThat(html).contains("public class Test {");
        assertThat(html).contains("    // code");
        assertThat(html).contains("}</code></pre>");
    }

    @Test
    void should_convert_unordered_lists() {
        // given
        String markdown = "- Item 1\n- Item 2\n- Item 3";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).contains("<ul>");
        assertThat(html).contains("<li>Item 1</li>");
        assertThat(html).contains("<li>Item 2</li>");
        assertThat(html).contains("<li>Item 3</li>");
        assertThat(html).contains("</ul>");
    }

    @Test
    void should_convert_ordered_lists() {
        // given
        String markdown = "1. Item 1\n2. Item 2\n3. Item 3";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).contains("<ol>");
        assertThat(html).contains("<li>Item 1</li>");
        assertThat(html).contains("<li>Item 2</li>");
        assertThat(html).contains("<li>Item 3</li>");
        assertThat(html).contains("</ol>");
    }

    @Test
    void should_convert_bold_and_italic_text() {
        // given
        String markdown = "This is **bold** and this is *italic*.";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).isEqualTo("<p>This is <b>bold</b> and this is <i>italic</i>.</p>");
    }

    @Test
    void should_convert_links() {
        // given
        String markdown = "This is a [link](https://example.com).";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).isEqualTo("<p>This is a <a href=\"https://example.com\">link</a>.</p>");
    }

    @Test
    void should_convert_url_tags() {
        // given
        String markdown = "Visit <https://example.com> for more information.";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).isEqualTo("<p>Visit <a href=\"https://example.com\">https://example.com</a> for more information.</p>");
    }

    @Test
    void should_convert_pmd_rule_links() {
        // given
        String markdown = "See [PMD rule](pmd_rules_java.html#rule1) for details.";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).isEqualTo("<p>See <a href=\"https://pmd.github.io/pmd/pmd_rules_java.html#rule1\">PMD rule</a> for details.</p>");
    }

    @Test
    void should_convert_note_italics() {
        // given
        String markdown = "_Note:_ This is important.";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // Print the actual output for debugging
        System.out.println("[DEBUG_LOG] Actual output: " + html);

        // then
        assertThat(html).isEqualTo("<p><b>Note:</b> This is important.</p>");
    }

    @Test
    void should_convert_sections() {
        // given
        String markdown = "Problem: This is a problem.\nSolution: This is a solution.";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).contains("<p><b>Problem:</b> This is a problem.</p>");
        assertThat(html).contains("<p><b>Solution:</b> This is a solution.</p>");
    }

    @Test
    void should_handle_complex_markdown() {
        // given
        String markdown = "# Title\n\nThis is a paragraph with `code` and **bold** text.\n\n- List item 1\n- List item 2\n\n```java\npublic class Test {}\n```";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).contains("<h1>Title</h1>");
        assertThat(html).contains("<p>This is a paragraph with <code>code</code> and <b>bold</b> text.</p>");
        assertThat(html).contains("<ul>");
        assertThat(html).contains("<li>List item 1</li>");
        assertThat(html).contains("<li>List item 2</li>");
        assertThat(html).contains("</ul>");
        assertThat(html).contains("<pre><code class=\"language-java\">");
        assertThat(html).contains("public class Test {}</code></pre>");
    }

    @Test
    void should_escape_html_in_code_blocks() {
        // given
        String markdown = "`<div>This should be escaped</div>`";

        // when
        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // then
        assertThat(html).isEqualTo("<p><code>&lt;div&gt;This should be escaped&lt;/div&gt;</code></p>");
    }

    @Test
    void should_convert_empty_camelCase_to_empty_string() {
        // given
        String camelCase = "";

        // when
        String readable = MarkdownToHtmlConverter.camelCaseToReadable(camelCase);

        // then
        assertThat(readable).isEmpty();
    }

    @Test
    void should_convert_null_camelCase_to_empty_string() {
        // when
        String readable = MarkdownToHtmlConverter.camelCaseToReadable(null);

        // then
        assertThat(readable).isEmpty();
    }

    @Test
    void should_convert_simple_camelCase() {
        // given
        String camelCase = "simpleTest";

        // when
        String readable = MarkdownToHtmlConverter.camelCaseToReadable(camelCase);

        // then
        assertThat(readable).isEqualTo("Simple test");
    }

    @Test
    void should_preserve_acronyms_in_camelCase() {
        // given
        String camelCase = "APITest";

        // when
        String readable = MarkdownToHtmlConverter.camelCaseToReadable(camelCase);

        // then
        assertThat(readable).isEqualTo("API test");
    }

    @Test
    void should_handle_multiple_acronyms_in_camelCase() {
        // given
        String camelCase = "XMLHTTPRequest";

        // when
        String readable = MarkdownToHtmlConverter.camelCaseToReadable(camelCase);

        // then
        assertThat(readable).isEqualTo("XMLHTTP request");
    }

    @Test
    void should_handle_numbers_in_camelCase() {
        // given
        String camelCase = "base64Encoder";

        // when
        String readable = MarkdownToHtmlConverter.camelCaseToReadable(camelCase);

        // then
        assertThat(readable).isEqualTo("Base64 encoder");
    }

    @Test
    void should_handle_special_case_NaN() {
        // given
        String camelCase = "isNaN";

        // when
        String readable = MarkdownToHtmlConverter.camelCaseToReadable(camelCase);

        // then
        assertThat(readable).isEqualTo("Is NaN");
    }

    @Test
    void should_handle_complex_camelCase() {
        // given
        String camelCase = "AbstractClassWithoutAbstractMethod";

        // when
        String readable = MarkdownToHtmlConverter.camelCaseToReadable(camelCase);

        // then
        assertThat(readable).isEqualTo("Abstract class without abstract method");
    }
}

/*
 * SonarQube PMD7 Plugin
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
}

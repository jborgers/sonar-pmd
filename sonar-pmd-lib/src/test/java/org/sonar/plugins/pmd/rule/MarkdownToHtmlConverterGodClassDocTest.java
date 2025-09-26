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

/**
 * Reproduces the ordering issue as seen in the DataClass rule description.
 * The second headline (paragraph) is placed before the first list.
 */
class MarkdownToHtmlConverterGodClassDocTest {

    @Test
    void paragraphs_should_precede_their_respective_lists() {
        String markdown = String.join("\n",
                "The rule uses metrics to implement its detection strategy. The violation message",
                "gives information about the values of these metrics:",
                "* WMC: a class complexity measure for a class, see { jdoc java::lang.java.metrics.JavaMetrics#WEIGHED_METHOD_COUNT }",
                "* WOC: a 'non-triviality' measure for a class, see { jdoc java::lang.java.metrics.JavaMetrics#WEIGHT_OF_CLASS }",
                "* NOPA: number of public attributes, see { jdoc java::lang.java.metrics.JavaMetrics#NUMBER_OF_PUBLIC_FIELDS }",
                "* NOAM: number of public accessor methods, see { jdoc java::lang.java.metrics.JavaMetrics#NUMBER_OF_ACCESSORS }",
                "",
                "The rule identifies a god class by looking for classes which have all of the following properties:",
                "* High NOPA + NOAM",
                "* Low WOC",
                "* Low WMC"
        );

        String html = MarkdownToHtmlConverter.convertToHtml(markdown);

        // The expected correct order is:
        // <p>...metrics...</p>
        // <ul>...first list...</ul>
        // <p>...identifies...</p>
        // <ul>...second list...</ul>
        int idxMetricsPara = html.indexOf("The rule uses metrics to implement its detection strategy.");
        int idxFirstUl = html.indexOf("<ul>");
        int idxIdentifiesPara = html.indexOf("The rule identifies a god class by looking for classes which have all of the following properties:");
        int idxSecondUl = html.indexOf("<ul>", idxFirstUl + 1);

        // Sanity: all parts must exist
        assertThat(idxMetricsPara).isGreaterThanOrEqualTo(0);
        assertThat(idxFirstUl).isGreaterThanOrEqualTo(0);
        assertThat(idxIdentifiesPara).isGreaterThanOrEqualTo(0);
        assertThat(idxSecondUl).isGreaterThanOrEqualTo(0);

        assertThat(idxFirstUl).as("Assert ordering: first list must come after first paragraph")
                .isGreaterThan(idxMetricsPara);

        assertThat(idxIdentifiesPara).as("Second paragraph must come after first list")
                .isGreaterThan(idxFirstUl);

        assertThat(idxSecondUl).as("And second list must come after second paragraph")
                .isGreaterThan(idxIdentifiesPara);
    }
}

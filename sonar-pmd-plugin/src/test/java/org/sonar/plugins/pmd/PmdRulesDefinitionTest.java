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

import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Param;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.plugins.pmd.rule.PmdRulesDefinition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PmdRulesDefinitionTest {

    @Test
    void test() {
        PmdRulesDefinition definition = new PmdRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();
        definition.define(context);
        RulesDefinition.Repository repository = context.repository(PmdConstants.MAIN_JAVA_REPOSITORY_KEY);

        assertThat(repository.name()).isEqualTo(PmdConstants.REPOSITORY_NAME);
        assertThat(repository.language()).isEqualTo(PmdConstants.LANGUAGE_JAVA_KEY);

        List<Rule> rules = repository.rules();

        assertThat(rules).hasSize(281);

        for (Rule rule : rules) {
            assertThat(rule.key()).isNotNull();
            assertThat(rule.internalKey()).isNotNull();
            assertThat(rule.name()).isNotNull();
            assertThat(rule.htmlDescription()).isNotNull();
            assertThat(rule.severity()).isNotNull();

            // Verify code blocks don't contain paragraph tags
            String description = rule.htmlDescription();
            if (description.contains("<pre><code")) {
                assertThat(description).doesNotContain("</p><p>");
            }

            for (Param param : rule.params()) {
                assertThat(param.name()).isNotNull();
                assertThat(param.description())
                        .overridingErrorMessage("Description is not set for parameter '" + param.name() + "' of rule '" + rule.key())
                        .isNotNull();
            }
        }
    }

}

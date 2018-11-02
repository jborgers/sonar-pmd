/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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

import java.util.List;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import org.sonar.api.PropertyType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Param;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.plugins.pmd.rule.PmdRulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class PmdRulesDefinitionTest {

    @Test
    void test() {
        PmdRulesDefinition definition = new PmdRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();
        definition.define(context);
        RulesDefinition.Repository repository = context.repository(PmdConstants.REPOSITORY_KEY);

        assertThat(repository.name()).isEqualTo(PmdConstants.REPOSITORY_NAME);
        assertThat(repository.language()).isEqualTo(PmdConstants.LANGUAGE_KEY);

        List<Rule> rules = repository.rules();
        assertThat(rules).hasSize(268);

        for (Rule rule : rules) {
            assertThat(rule.key()).isNotNull();
            assertThat(rule.internalKey()).isNotNull();
            assertThat(rule.name()).isNotNull();
            assertThat(rule.htmlDescription()).isNotNull();
            assertThat(rule.severity()).isNotNull();

            for (Param param : rule.params()) {
                assertThat(param.name()).isNotNull();
                assertThat(param.description())
                        .overridingErrorMessage("Description is not set for parameter '" + param.name() + "' of rule '" + rule.key())
                        .isNotNull();
            }
        }
    }

    @Test
    void should_exclude_junit_rules() {
        PmdRulesDefinition definition = new PmdRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();
        definition.define(context);
        RulesDefinition.Repository repository = context.repository(PmdConstants.REPOSITORY_KEY);

        for (Rule rule : repository.rules()) {
            assertThat(rule.key()).doesNotContain("JUnitStaticSuite");
        }
    }

    @Test
    void should_use_text_parameter_for_xpath_rule() {
        PmdRulesDefinition definition = new PmdRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();
        definition.define(context);
        RulesDefinition.Repository repository = context.repository(PmdConstants.REPOSITORY_KEY);

        Rule xpathRule = Iterables.find(repository.rules(), rule -> rule.key().equals("XPathRule"));

        assertThat(xpathRule.param("xpath").type().type()).isEqualTo(PropertyType.TEXT.name());
    }
}

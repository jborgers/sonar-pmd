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

package org.sonar.plugins.pmd.rule;

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.server.rule.RulesDefinition.Repository;
import org.sonar.api.server.rule.RulesDefinition.Rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


/**
 * Taken from <code>sslr-squid-bridge:org.sonar.squidbridge.rules.ExternalDescriptionLoaderTest</code>.
 */
class ExternalDescriptionLoaderTest {

    private static final String REPO_KEY = "repoKey";
    private static final String LANGUAGE_KEY = "languageKey";

    private RulesDefinition.Context context = new RulesDefinition.Context();
    private NewRepository repository = context.createRepository(REPO_KEY, LANGUAGE_KEY);

    @Test
    void existing_rule_description() {

        // given
        repository.createRule("ruleWithExternalInfo").setName("name1");

        // when
        Rule rule = buildRepository().rule("ruleWithExternalInfo");

        // then
        assertThat(rule)
                .isNotNull()
                .hasFieldOrPropertyWithValue("htmlDescription", "description for ruleWithExternalInfo");
    }

    @Test
    void rule_with_non_external_description() {

        // given
        repository.createRule("ruleWithoutExternalInfo").setName("name1").setHtmlDescription("my description");

        // when
        final Rule rule = buildRepository().rule("ruleWithoutExternalInfo");

        // then
        assertThat(rule)
                .isNotNull()
                .hasFieldOrPropertyWithValue("htmlDescription", "my description");
    }

    @Test
    void rule_without_description() {

        // given
        repository.createRule("ruleWithoutExternalInfo").setName("name1");

        // when
        final Throwable thrown = catchThrowable(() -> buildRepository().rule("ruleWithoutExternalInfo"));

        // then
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void invalid_url() {

        // given
        final ExternalDescriptionLoader loader = new ExternalDescriptionLoader(LANGUAGE_KEY);
        final NewRule rule = repository.createRule("ruleWithoutExternalInfo").setName("name1");

        // when
        final Throwable thrown = catchThrowable(() -> loader.addHtmlDescription(rule, new URL("file:///xx/yy")));

        // then
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }

    private Repository buildRepository() {
        ExternalDescriptionLoader.loadHtmlDescriptions(repository, "/org/sonar/l10n/languageKey/rules/repoKey");
        repository.done();
        return context.repository(REPO_KEY);
    }

}
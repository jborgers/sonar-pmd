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

import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.server.rule.RulesDefinition.Repository;
import org.sonar.api.server.rule.RulesDefinition.Rule;

import static org.fest.assertions.Assertions.assertThat;


/**
 * Taken from <code>sslr-squid-bridge:org.sonar.squidbridge.rules.ExternalDescriptionLoaderTest</code>.
 */
public class ExternalDescriptionLoaderTest {

    private static final String REPO_KEY = "repoKey";
    private static final String LANGUAGE_KEY = "languageKey";

    private RulesDefinition.Context context = new RulesDefinition.Context();
    private NewRepository repository = context.createRepository(REPO_KEY, LANGUAGE_KEY);

    @Test
    public void existing_rule_description() {
        repository.createRule("ruleWithExternalInfo").setName("name1");
        Rule rule = buildRepository().rule("ruleWithExternalInfo");
        assertThat(rule.htmlDescription()).isEqualTo("description for ruleWithExternalInfo");
    }

    @Test
    public void rule_with_non_external_description() {
        repository.createRule("ruleWithoutExternalInfo").setName("name1").setHtmlDescription("my description");
        Rule rule = buildRepository().rule("ruleWithoutExternalInfo");
        assertThat(rule.htmlDescription()).isEqualTo("my description");
    }

    @Test(expected = IllegalStateException.class)
    public void rule_without_description() {
        repository.createRule("ruleWithoutExternalInfo").setName("name1");
        buildRepository().rule("ruleWithoutExternalInfo");
    }

    @Test(expected = IllegalStateException.class)
    public void invalid_url() throws Exception {
        ExternalDescriptionLoader loader = new ExternalDescriptionLoader(LANGUAGE_KEY);
        NewRule rule = repository.createRule("ruleWithoutExternalInfo").setName("name1");
        loader.addHtmlDescription(rule, new URL("file:///xx/yy"));
    }

    private Repository buildRepository() {
        ExternalDescriptionLoader.loadHtmlDescriptions(repository, "/org/sonar/l10n/languageKey/rules/repoKey");
        repository.done();
        return context.repository(REPO_KEY);
    }

}
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
package org.sonar.plugins.pmd.profile;

import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.pmd.PmdTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PmdProfileImporterTest {

    private PmdProfileImporter importer;
    private ValidationMessages messages;

    private static Reader read(String path) {
        return new StringReader(PmdTestUtils.getResourceContent(path));
    }

    private static RuleFinder createRuleFinder() {
        RuleFinder ruleFinder = mock(RuleFinder.class);
        when(ruleFinder.find(any(RuleQuery.class))).then((Answer<Rule>) invocation -> {
            RuleQuery query = (RuleQuery) invocation.getArguments()[0];
            String configKey = query.getConfigKey();
            String key = configKey.substring(configKey.lastIndexOf('/') + 1);
            Rule rule = Rule.create(query.getRepositoryKey(), key, "").setConfigKey(configKey).setSeverity(RulePriority.BLOCKER);
            if (rule.getConfigKey().equals("category/java/bestpractices.xml/ForLoopVariableCount")) {
                rule.createParameter("maximumVariables");
            }
            return rule;
        });
        return ruleFinder;
    }

    @BeforeEach
    void setUpImporter() {
        messages = ValidationMessages.create();
        importer = new PmdProfileImporter(createRuleFinder());
    }

    @Test
    void should_import_simple_profile() {
        Reader reader = read("/org/sonar/plugins/pmd/simple.xml");

        RulesProfile profile = importer.importProfile(reader, messages);

        assertThat(profile.getActiveRules()).hasSize(3);
        assertThat(profile.getActiveRuleByConfigKey("pmd", "category/java/errorprone.xml/AvoidLiteralsInIfCondition")).isNotNull();
        assertThat(profile.getActiveRuleByConfigKey("pmd", "category/java/multithreading.xml/DoubleCheckedLocking")).isNotNull();
        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    void should_import_profile_with_xpath_rule() {
        Reader reader = read("/org/sonar/plugins/pmd/export_xpath_rules.xml");

        RulesProfile profile = importer.importProfile(reader, messages);

        assertThat(profile.getActiveRules()).isEmpty();
        assertThat(messages.hasWarnings()).isTrue();
    }

    @Test
    void should_import_parameter() {
        Reader reader = read("/org/sonar/plugins/pmd/simple.xml");

        RulesProfile profile = importer.importProfile(reader, messages);
        ActiveRule activeRule = profile.getActiveRuleByConfigKey("pmd", "category/java/bestpractices.xml/ForLoopVariableCount");

        assertThat(activeRule.getParameter("maximumVariables")).isEqualTo("5");
    }

    @Test
    void should_import_default_priority() {
        Reader reader = read("/org/sonar/plugins/pmd/simple.xml");

        RulesProfile profile = importer.importProfile(reader, messages);
        ActiveRule activeRule = profile.getActiveRuleByConfigKey("pmd", "category/java/multithreading.xml/DoubleCheckedLocking");

        assertThat(activeRule.getSeverity()).isSameAs(RulePriority.BLOCKER);
    }

    @Test
    void should_import_priority() {
        Reader reader = read("/org/sonar/plugins/pmd/simple.xml");

        RulesProfile profile = importer.importProfile(reader, messages);

        ActiveRule activeRule = profile.getActiveRuleByConfigKey("pmd", "category/java/errorprone.xml/AvoidLiteralsInIfCondition");
        assertThat(activeRule.getSeverity()).isSameAs(RulePriority.CRITICAL);

        activeRule = profile.getActiveRuleByConfigKey("pmd", "category/java/bestpractices.xml/ForLoopVariableCount");
        assertThat(activeRule.getSeverity()).isSameAs(RulePriority.MINOR);
    }

    @Test
    void should_import_pmd_configuration_with_unknown_nodes() {
        Reader reader = read("/org/sonar/plugins/pmd/complex-with-unknown-nodes.xml");

        RulesProfile profile = importer.importProfile(reader, messages);

        assertThat(profile.getActiveRules()).hasSize(3);
    }

    @Test
    void should_deal_with_unsupported_property() {
        Reader reader = read("/org/sonar/plugins/pmd/simple.xml");

        RulesProfile profile = importer.importProfile(reader, messages);
        ActiveRule check = profile.getActiveRuleByConfigKey("pmd", "category/java/bestpractices.xml/ForLoopVariableCount");

        // PMD7-MIGRATION what is meaning of this check? The list of parameters is empty (but expected maximumVariables?)
        // in the errors: The property 'maximumVariables' is not supported in the pmd rule: category/java/bestpractices.xml/ForLoopVariableCount
        assertThat(check.getParameter("threshold")).isNull();
        assertThat(messages.getWarnings()).hasSize(2);
    }

    @Test
    void should_fail_on_invalid_xml() {
        Reader reader = new StringReader("not xml");

        importer.importProfile(reader, messages);

        assertThat(messages.getErrors()).hasSize(1);
    }

    @Test
    void should_warn_on_unknown_rule() {
        Reader reader = read("/org/sonar/plugins/pmd/simple.xml");

        importer = new PmdProfileImporter(mock(RuleFinder.class));
        RulesProfile profile = importer.importProfile(reader, messages);

        assertThat(profile.getActiveRules()).isEmpty();
        assertThat(messages.getWarnings()).hasSize(4);
    }
}

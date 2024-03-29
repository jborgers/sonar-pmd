/*
 * SonarQube PMD Plugin
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
package org.sonar.plugins.pmd.xml.factory;

import java.util.*;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.plugins.pmd.PmdPriorities;
import org.sonar.plugins.pmd.xml.PmdProperty;
import org.sonar.plugins.pmd.xml.PmdRule;
import org.sonar.plugins.pmd.xml.PmdRuleSet;

/**
 * Factory class to create {@link org.sonar.plugins.pmd.xml.PmdRuleSet} out of {@link org.sonar.api.batch.rule.ActiveRules}.
 */
public class ActiveRulesRuleSetFactory implements RuleSetFactory {

    private final ActiveRules activeRules;
    private final String repositoryKey;

    public ActiveRulesRuleSetFactory(ActiveRules activeRules, String repositoryKey) {
        this.activeRules = activeRules;
        this.repositoryKey = repositoryKey;
    }

    @Override
    public PmdRuleSet create() {

        final Collection<ActiveRule> rules = this.activeRules.findByRepository(repositoryKey);
        PmdRuleSet ruleset = new PmdRuleSet();
        ruleset.setName(repositoryKey);
        ruleset.setDescription(String.format("Sonar Profile: %s", repositoryKey));
        for (ActiveRule rule : rules) {
            String configKey = rule.internalKey();
            PmdRule pmdRule = new PmdRule(configKey, PmdPriorities.ofSonarRule(rule));
            addRuleProperties(rule, pmdRule);
            ruleset.addRule(pmdRule);

            pmdRule.processXpath(rule.ruleKey().rule());
        }
        return ruleset;
    }

    private void addRuleProperties(org.sonar.api.batch.rule.ActiveRule activeRule, PmdRule pmdRule) {
        if ((activeRule.params() != null) && !activeRule.params().isEmpty()) {
            List<PmdProperty> properties = new ArrayList<>();
            for (Map.Entry<String, String> activeRuleParam : activeRule.params().entrySet()) {
                properties.add(new PmdProperty(activeRuleParam.getKey(), activeRuleParam.getValue()));
            }
            pmdRule.setProperties(properties);
        }
    }

    @Override
    public void close() {
        // Unnecessary in this class.
    }
}

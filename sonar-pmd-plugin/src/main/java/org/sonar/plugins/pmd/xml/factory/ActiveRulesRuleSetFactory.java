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
package org.sonar.plugins.pmd.xml.factory;

import java.util.*;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.rule.RuleScope;
import org.sonar.plugins.pmd.PmdPriorities;
import org.sonar.plugins.pmd.rule.PmdRuleScopeRegistry;
import org.sonar.plugins.pmd.xml.PmdProperty;
import org.sonar.plugins.pmd.xml.PmdRule;
import org.sonar.plugins.pmd.xml.PmdRuleSet;

/**
 * Factory class to create {@link org.sonar.plugins.pmd.xml.PmdRuleSet} out of {@link org.sonar.api.batch.rule.ActiveRules}.
 */
public class ActiveRulesRuleSetFactory implements RuleSetFactory {

    private final ActiveRules activeRules;
    private final String repositoryKey;
    private final RuleScope targetScope;
    private final PmdRuleScopeRegistry scopeRegistry;

    public ActiveRulesRuleSetFactory(ActiveRules activeRules, String repositoryKey, RuleScope targetScope, PmdRuleScopeRegistry scopeRegistry) {
        this.activeRules = activeRules;
        this.repositoryKey = repositoryKey;
        this.targetScope = targetScope;
        this.scopeRegistry = scopeRegistry;
    }

    @Override
    public PmdRuleSet create() {

        final Collection<ActiveRule> rules = this.activeRules.findByRepository(repositoryKey);
        PmdRuleSet ruleset = new PmdRuleSet();
        ruleset.setName(repositoryKey);
        ruleset.setDescription("Sonar Profile: " + repositoryKey + " (" + targetScope + ")");
        for (ActiveRule rule : rules) {
            if (!isRuleInScope(rule, targetScope)) {
                continue;
            }
            String configKey = rule.internalKey();
            PmdRule pmdRule = new PmdRule(configKey, PmdPriorities.ofSonarRule(rule));
            addRuleProperties(rule, pmdRule);
            ruleset.addRule(pmdRule);

            pmdRule.processXpath(rule.ruleKey().rule());
        }
        return ruleset;
    }

    private boolean isRuleInScope(ActiveRule activeRule, RuleScope targetScope) {
        if (targetScope == RuleScope.ALL) {
            return true;
        }

        // Get the actual scope from the registry (based on XML rule definition)
        RuleScope ruleScope = scopeRegistry.getScope(activeRule.ruleKey().rule());

        // Rule matches if:
        // - Rule scope is ALL (applies to both MAIN and TEST)
        // - Rule scope matches the target scope exactly
        return ruleScope == RuleScope.ALL || ruleScope == targetScope;
    }

    private void addRuleProperties(ActiveRule activeRule, PmdRule pmdRule) {
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

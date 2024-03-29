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

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.plugins.pmd.PmdPriorities;
import org.sonar.plugins.pmd.xml.PmdProperty;
import org.sonar.plugins.pmd.xml.PmdRule;
import org.sonar.plugins.pmd.xml.PmdRuleSet;

/**
 * Factory class to create {@link org.sonar.plugins.pmd.xml.PmdRuleSet} out of {@link org.sonar.api.profiles.RulesProfile}.
 */
public class RulesProfileRuleSetFactory implements RuleSetFactory {

    private final RulesProfile rulesProfile;
    private final String repositoryKey;

    public RulesProfileRuleSetFactory(RulesProfile rulesProfile, String repositoryKey) {
        this.rulesProfile = rulesProfile;
        this.repositoryKey = repositoryKey;
    }

    @Override
    public PmdRuleSet create() {

        final PmdRuleSet ruleset = new PmdRuleSet();
        ruleset.setName(repositoryKey);
        ruleset.setDescription(String.format("Sonar Profile: %s", repositoryKey));

        final List<ActiveRule> activeRules = rulesProfile.getActiveRulesByRepository(repositoryKey);

        for (ActiveRule activeRule : activeRules) {
            if (activeRule.getRule().getRepositoryKey().equals(repositoryKey)) {
                String configKey = activeRule.getRule().getConfigKey();
                PmdRule rule = new PmdRule(configKey, PmdPriorities.fromSonarPrio(activeRule.getSeverity()));
                addRuleProperties(activeRule, rule);
                ruleset.addRule(rule);
                rule.processXpath(activeRule.getRuleKey());
            }
        }

        return ruleset;
    }

    private void addRuleProperties(ActiveRule activeRule, PmdRule pmdRule) {
        if ((activeRule.getActiveRuleParams() != null) && !activeRule.getActiveRuleParams().isEmpty()) {
            List<PmdProperty> properties = new ArrayList<>();
            for (ActiveRuleParam activeRuleParam : activeRule.getActiveRuleParams()) {
                properties.add(new PmdProperty(activeRuleParam.getRuleParam().getKey(), activeRuleParam.getValue()));
            }
            pmdRule.setProperties(properties);
        }
    }

    @Override
    public void close() {
        // Unnecessary in this class.
    }
}

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

        RuleScope ruleScope = scopeRegistry.getScope(activeRule.ruleKey().rule());

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
        // no-op
    }
}

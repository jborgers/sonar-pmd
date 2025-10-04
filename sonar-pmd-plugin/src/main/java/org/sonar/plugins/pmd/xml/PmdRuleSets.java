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
package org.sonar.plugins.pmd.xml;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.pmd.rule.PmdRuleScopeRegistry;
import org.sonar.plugins.pmd.xml.factory.ActiveRulesRuleSetFactory;
import org.sonar.plugins.pmd.xml.factory.RuleSetFactory;
import org.sonar.plugins.pmd.xml.factory.XmlRuleSetFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * Convenience class that creates {@link PmdRuleSet} instances out of the given input.
 */
public class PmdRuleSets {

    private static final Logger LOG = LoggerFactory.getLogger(PmdRuleSets.class);

    private static final PmdRuleScopeRegistry SCOPE_REGISTRY = createRegistry();

    private static PmdRuleScopeRegistry createRegistry() {
        try {
            return new PmdRuleScopeRegistry(
                    "/org/sonar/plugins/pmd/rules-java.xml",
                    "/org/sonar/plugins/pmd/rules-kotlin.xml"
            );
        } catch (Exception e) {
            LOG.error("Failed to initialize PMD scope registry, using empty registry", e);
            return new PmdRuleScopeRegistry(); // Empty or minimal fallback
        }
    }

    private static PmdRuleScopeRegistry getScopeRegistry() {
        return SCOPE_REGISTRY;
    }

    private PmdRuleSets() {}

    /**
     * @param configReader A character stream containing the data of the {@link PmdRuleSet}.
     * @param messages     SonarQube validation messages - allow to inform the enduser about processing problems.
     * @return An instance of PmdRuleSet. The output may be empty but never null.
     */
    public static PmdRuleSet from(Reader configReader, ValidationMessages messages) {
        return createQuietly(new XmlRuleSetFactory(configReader, messages));
    }

    /**
     * @param activeRules   The currently active rules.
     * @param repositoryKey The key identifier of the rule repository.
     * @return An instance of PmdRuleSet. The output may be empty but never null.
     */
    public static PmdRuleSet from(ActiveRules activeRules, String repositoryKey) {
        return from(activeRules, repositoryKey, RuleScope.ALL);
    }

    public static PmdRuleSet from(ActiveRules activeRules, String repositoryKey, RuleScope scope) {
        return create(new ActiveRulesRuleSetFactory(activeRules, repositoryKey, scope, getScopeRegistry()));
    }

    private static PmdRuleSet create(RuleSetFactory factory) {
        return factory.create();
    }

    private static PmdRuleSet createQuietly(XmlRuleSetFactory factory) {

        final PmdRuleSet result = create(factory);

        try {
            factory.close();
        } catch (IOException e) {
            LOG.warn("Failed to close the given resource.", e);
        }

        return result;
    }
}

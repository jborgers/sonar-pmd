/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2021 SonarSource SA and others
 * mailto:jens AT gerdes DOT digital
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

import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.pmd.xml.factory.ActiveRulesRuleSetFactory;
import org.sonar.plugins.pmd.xml.factory.RuleSetFactory;
import org.sonar.plugins.pmd.xml.factory.RulesProfileRuleSetFactory;
import org.sonar.plugins.pmd.xml.factory.XmlRuleSetFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * Convenience class that creates {@link PmdRuleSet} instances out of the given input.
 */
public class PmdRuleSets {

    private static final Logger LOG = Loggers.get(PmdRuleSets.class);

    private PmdRuleSets() {
    }

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
        return create(new ActiveRulesRuleSetFactory(activeRules, repositoryKey));
    }

    /**
     * @param rulesProfile  The current rulesprofile.
     * @param repositoryKey The key identifier of the rule repository.
     * @return An instance of PmdRuleSet. The output may be empty but never null.
     */
    public static PmdRuleSet from(RulesProfile rulesProfile, String repositoryKey) {
        return create(new RulesProfileRuleSetFactory(rulesProfile, repositoryKey));
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

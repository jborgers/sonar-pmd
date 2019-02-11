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
package org.sonar.plugins.pmd.profile;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.pmd.PmdConstants;
import org.sonar.plugins.pmd.PmdLevelUtils;
import org.sonar.plugins.pmd.xml.PmdProperty;
import org.sonar.plugins.pmd.xml.PmdRule;
import org.sonar.plugins.pmd.xml.PmdRuleSet;

public class PmdProfileExporter extends ProfileExporter {

    private static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";
    private static final Logger LOG = Loggers.get(PmdProfileExporter.class);

    public PmdProfileExporter() {
        super(PmdConstants.REPOSITORY_KEY, PmdConstants.PLUGIN_NAME);
        setSupportedLanguages(PmdConstants.LANGUAGE_KEY);
        setMimeType(CONTENT_TYPE_APPLICATION_XML);
    }

    private static void addRuleProperties(ActiveRule activeRule, PmdRule pmdRule) {
        if ((activeRule.getActiveRuleParams() != null) && !activeRule.getActiveRuleParams().isEmpty()) {
            List<PmdProperty> properties = new ArrayList<>();
            for (ActiveRuleParam activeRuleParam : activeRule.getActiveRuleParams()) {
                properties.add(new PmdProperty(activeRuleParam.getRuleParam().getKey(), activeRuleParam.getValue()));
            }
            pmdRule.setProperties(properties);
        }
    }

    private static void addRuleProperties(org.sonar.api.batch.rule.ActiveRule activeRule, PmdRule pmdRule) {
        if ((activeRule.params() != null) && !activeRule.params().isEmpty()) {
            List<PmdProperty> properties = new ArrayList<>();
            for (Map.Entry<String, String> activeRuleParam : activeRule.params().entrySet()) {
                properties.add(new PmdProperty(activeRuleParam.getKey(), activeRuleParam.getValue()));
            }
            pmdRule.setProperties(properties);
        }
    }

    static void processXPathRule(String sonarRuleKey, PmdRule rule) {
        if (PmdConstants.XPATH_CLASS.equals(rule.getRef())) {
            rule.setRef(null);
            PmdProperty xpathMessage = rule.getProperty(PmdConstants.XPATH_MESSAGE_PARAM);
            if (xpathMessage == null) {
                throw new IllegalArgumentException("Property '" + PmdConstants.XPATH_MESSAGE_PARAM + "' should be set for PMD rule " + sonarRuleKey);
            }
            rule.setMessage(xpathMessage.getValue());
            rule.removeProperty(PmdConstants.XPATH_MESSAGE_PARAM);
            PmdProperty xpathExp = rule.getProperty(PmdConstants.XPATH_EXPRESSION_PARAM);
            if (xpathExp == null) {
                throw new IllegalArgumentException("Property '" + PmdConstants.XPATH_EXPRESSION_PARAM + "' should be set for PMD rule " + sonarRuleKey);
            }
            xpathExp.setCdataValue(xpathExp.getValue());
            rule.setClazz(PmdConstants.XPATH_CLASS);
            rule.setLanguage(PmdConstants.LANGUAGE_KEY);
            rule.setName(sonarRuleKey);
        }
    }

    @Override
    public void exportProfile(RulesProfile profile, Writer writer) {
        final String profileName = profile.getName();
        final PmdRuleSet tree = createPmdRuleset(PmdConstants.REPOSITORY_KEY, profile.getActiveRulesByRepository(PmdConstants.REPOSITORY_KEY));

        try {
            tree.writeTo(writer);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("An exception occurred while generating the PMD configuration file from profile: " + profileName, e);
        }
    }

    public static String exportProfileFromScannerSide(String repositoryKey, ActiveRules profile) {
        PmdRuleSet tree = createPmdRulesetB(repositoryKey, profile.findByRepository(repositoryKey));
        StringWriter stringWriter = new StringWriter();
        tree.writeTo(stringWriter);
        return stringWriter.toString();
    }

    private static PmdRuleSet createPmdRulesetB(String repositoryKey, Collection<org.sonar.api.batch.rule.ActiveRule> activeRules) {
        PmdRuleSet ruleset = new PmdRuleSet();
        ruleset.setName(repositoryKey);
        ruleset.setDescription(String.format("Sonar Profile: %s", repositoryKey));
        for (org.sonar.api.batch.rule.ActiveRule activeRule : activeRules) {
                String configKey = activeRule.internalKey();
                PmdRule rule = new PmdRule(configKey, PmdLevelUtils.toLevel(RulePriority.valueOfString(activeRule.severity())));
                addRuleProperties(activeRule, rule);
                ruleset.addRule(rule);
                processXPathRule(activeRule.internalKey(), rule);
        }
        return ruleset;
    }

    private static PmdRuleSet createPmdRuleset(String repositoryKey, List<ActiveRule> activeRules) {
        PmdRuleSet ruleset = new PmdRuleSet();
        ruleset.setName(repositoryKey);
        ruleset.setDescription(String.format("Sonar Profile: %s", repositoryKey));
        for (ActiveRule activeRule : activeRules) {
            if (activeRule.getRule().getRepositoryKey().equals(repositoryKey)) {
                String configKey = activeRule.getRule().getConfigKey();
                PmdRule rule = new PmdRule(configKey, PmdLevelUtils.toLevel(activeRule.getSeverity()));
                addRuleProperties(activeRule, rule);
                ruleset.addRule(rule);
                processXPathRule(activeRule.getRuleKey(), rule);
            }
        }
        return ruleset;
    }
}

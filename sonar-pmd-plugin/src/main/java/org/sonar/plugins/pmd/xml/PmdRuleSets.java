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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Convenience class that creates {@link PmdRuleSet} instances out of the given input.
 */
public class PmdRuleSets {

    private static final Logger LOG = LoggerFactory.getLogger(PmdRuleSets.class);

    private static final PmdRuleScopeRegistry SCOPE_REGISTRY = createRegistry();

    private static PmdRuleScopeRegistry createRegistry() {
        try {
            PmdRuleScopeRegistry registry = PmdRuleScopeRegistry.getInstance();
            registry.addXmlResources(
                    "/org/sonar/plugins/pmd/rules-java.xml",
                    "/org/sonar/plugins/pmd/rules-kotlin.xml"
            );

            ClassLoader cl = PmdRuleSets.class.getClassLoader();

            // Also support a generic index file that child plugins can ship to declare arbitrary rule XML paths
            String indexResource = "META-INF/sonar-pmd/scope-index.txt"; // each line: a classpath resource path to an XML
            try {
                Enumeration<URL> indexUrls = cl.getResources(indexResource);
                int processedIndexes = 0;
                List<URL> declaredRuleXmls = new ArrayList<>();
                while (indexUrls.hasMoreElements()) {
                    processedIndexes++;
                    URL idxUrl = indexUrls.nextElement();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(idxUrl.openStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            String path = line.trim();
                            if (path.isEmpty() || path.startsWith("#")) {
                                continue;
                            }
                            // Normalize leading slash for ClassLoader lookups
                            if (path.startsWith("/")) {
                                path = path.substring(1);
                            }
                            try {
                                Enumeration<URL> xmls = cl.getResources(path);
                                while (xmls.hasMoreElements()) {
                                    declaredRuleXmls.add(xmls.nextElement());
                                }
                            } catch (IOException e) {
                                LOG.warn("Failed to resolve declared PMD scope resource '{}' from index {}", path, idxUrl);
                            }
                        }
                    } catch (IOException e) {
                        LOG.warn("Failed to read PMD scope index {}", idxUrl, e);
                    }
                }
                if (!declaredRuleXmls.isEmpty()) {
                    LOG.info("Loading PMD scope definitions from {} resource(s) declared in {} index file(s)", declaredRuleXmls.size(), processedIndexes);
                    registry.addXmlUrls(declaredRuleXmls.toArray(new URL[0]));
                } else if (processedIndexes > 0) {
                    LOG.debug("No PMD scope resources declared in {} index file(s)", processedIndexes);
                }
            } catch (IOException e) {
                LOG.warn("Failed to enumerate PMD scope index files on classpath", e);
            }

            // Fallback: support well-known external PMD plugin rulesets if present on classpath
            // e.g., sonar-pmd-jpinpoint provides: com/jpinpoint/pmd/rules/jpinpoint-rules.xml
            try {
                String jpinpointPath = "com/jpinpoint/pmd/rules/jpinpoint-rules.xml";
                Enumeration<URL> jpUrls = cl.getResources(jpinpointPath);
                List<URL> found = new ArrayList<>();
                while (jpUrls.hasMoreElements()) {
                    found.add(jpUrls.nextElement());
                }
                if (!found.isEmpty()) {
                    LOG.info("Loading PMD scope definitions from jPinpoint ruleset found at {} location(s)", found.size());
                    registry.addXmlUrls(found.toArray(new URL[0]));
                }
            } catch (IOException e) {
                LOG.debug("Could not enumerate jPinpoint ruleset on classpath", e);
            }

            return registry;
        } catch (Exception e) {
            LOG.error("Failed to initialize PMD scope registry, using empty registry", e);
            return PmdRuleScopeRegistry.getInstance(); // Empty or minimal fallback
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

/*
 * Shared in lib to avoid duplication across modules.
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

public class PmdRuleSets {

    private static final Logger LOG = LoggerFactory.getLogger(PmdRuleSets.class);

    private static final PmdRuleScopeRegistry SCOPE_REGISTRY = createRegistry();

    private static PmdRuleScopeRegistry createRegistry() {
        try {
            PmdRuleScopeRegistry registry = PmdRuleScopeRegistry.getInstance();

            ClassLoader cl = PmdRuleSets.class.getClassLoader();

            loadPmdJavaAndKotlineRules(cl, registry);

            loadChildPmdPluginProvidedSonarRules(cl, registry);

            loadJPinpointPluginRules(cl, registry);

            return registry;
        } catch (Exception e) {
            LOG.error("Failed to initialize PMD scope registry, using empty registry", e);
            return PmdRuleScopeRegistry.getInstance();
        }
    }

    private static void loadPmdJavaAndKotlineRules(ClassLoader cl, PmdRuleScopeRegistry registry) {
        // Only load Java/Kotlin rule-scope resources if they exist on the classpath
        String javaPath = "org/sonar/plugins/pmd/rules-java.xml";
        String kotlinPath = "org/sonar/plugins/pmd/rules-kotlin.xml";
        if (cl.getResource(javaPath) != null) {
            registry.addXmlResources("/" + javaPath);
        }
        if (cl.getResource(kotlinPath) != null) {
            registry.addXmlResources("/" + kotlinPath);
        }
    }

    /**
     * Needed for backwards compatibility for the child PMD plugin with JPinpoint rules
     * Can be removed when the plugin has its own META-INF/sonar-pmd/sonar-pmd-rules-paths.txt
     */
    private static void loadJPinpointPluginRules(ClassLoader cl, PmdRuleScopeRegistry registry) {
        try {
            String jpinpointPath = "com/jpinpoint/sonar/rules/sonar-pmd-jpinpoint.xml";
            List<URL> found = new ArrayList<>();
            Enumeration<URL> jpUrls = cl.getResources(jpinpointPath);
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
    }

    private static void loadChildPmdPluginProvidedSonarRules(ClassLoader cl, PmdRuleScopeRegistry registry) {
        String indexResource = "META-INF/sonar-pmd/sonar-pmd-rules-paths.txt";
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
                        processSonarPluginRulesPathLine(cl, line, declaredRuleXmls, idxUrl);
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
    }

    private static void processSonarPluginRulesPathLine(ClassLoader cl, String line, List<URL> declaredRuleXmls, URL idxUrl) {
        String path = line.trim();
        if (path.isEmpty() || path.startsWith("#")) {
            return;
        }
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

    private PmdRuleSets() {}

    public static PmdRuleSet from(Reader configReader, ValidationMessages messages) {
        return createQuietly(new XmlRuleSetFactory(configReader, messages));
    }

    public static PmdRuleSet from(ActiveRules activeRules, String repositoryKey) {
        return from(activeRules, repositoryKey, RuleScope.ALL);
    }

    public static PmdRuleSet from(ActiveRules activeRules, String repositoryKey, RuleScope scope) {
        return create(new ActiveRulesRuleSetFactory(activeRules, repositoryKey, scope, SCOPE_REGISTRY));
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

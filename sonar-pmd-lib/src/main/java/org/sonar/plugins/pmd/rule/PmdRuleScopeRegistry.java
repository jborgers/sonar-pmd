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
package org.sonar.plugins.pmd.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rule.RuleScope;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.net.URL;

/**
 * Registry that stores the scope (MAIN, TEST, ALL) for each PMD rule by parsing the XML rule definitions.
 * This allows accurate scope determination during batch analysis without relying on heuristics.
 * Uses the same scope determination logic as {@link RulesDefinitionXmlLoader}.
 */
public class PmdRuleScopeRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PmdRuleScopeRegistry.class);

    private static final String ELEMENT_RULE = "rule";

    private final Map<String, RuleScope> ruleScopeMap = new HashMap<>();
    private final Set<String> loadedResources = new HashSet<>();

    private static volatile PmdRuleScopeRegistry INSTANCE;

    public static PmdRuleScopeRegistry getInstance() {
        if (INSTANCE == null) {
            synchronized (PmdRuleScopeRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PmdRuleScopeRegistry();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Creates an empty registry. Use addXmlResources(...) to load rule scopes.
     */
    public PmdRuleScopeRegistry() {
        // empty
    }

    public synchronized void addXmlResources(String... xmlResourcePaths) {
        if (xmlResourcePaths == null) {
            return;
        }
        for (String path : xmlResourcePaths) {
            LOGGER.info("Loading rule scopes from XML '{}'", path);
            if (path == null) {
                continue;
            }
            if (loadedResources.add(path)) { // only load once
                loadRulesFromXml(path);
            } else {
                LOGGER.debug("Rule scopes for XML '{}' already loaded, skipping.", path);
            }
        }
    }

    /**
     * Add XML resources provided as URLs, allowing multiple resources with the same path
     * to be loaded from different classpath entries (e.g., child plugins).
     */
    public synchronized void addXmlUrls(URL... urls) {
        if (urls == null) {
            return;
        }
        for (URL url : urls) {
            if (url == null) {
                continue;
            }
            String id = url.toExternalForm();
            if (loadedResources.add(id)) {
                LOGGER.info("Loading rule scopes from URL '{}'", id);
                loadRulesFromUrl(url);
            } else {
                LOGGER.debug("Rule scopes for URL '{}' already loaded, skipping.", id);
            }
        }
    }


    /**
     * Gets the scope for a rule key.
     *
     * @param ruleKey The rule key
     * @return The rule scope, or RuleScope.ALL if not found
     */
    public RuleScope getScope(String ruleKey) {
        return ruleScopeMap.getOrDefault(ruleKey, RuleScope.ALL);
    }

    private void loadRulesFromXml(String xmlResourcePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(xmlResourcePath)) {
            if (inputStream == null) {
                LOGGER.warn("Cannot find XML resource: {}", xmlResourcePath);
                return;
            }

            Map<String, RuleScope> scopes = loadRuleScopesFromStream(inputStream, StandardCharsets.UTF_8);
            ruleScopeMap.putAll(scopes);

            LOGGER.debug("Loaded {} rule scopes from {}", scopes.size(), xmlResourcePath);
        } catch (Exception e) {
            LOGGER.error("Failed to load rule scopes from {}", xmlResourcePath, e);
        }
    }

    private void loadRulesFromUrl(URL url) {
        try (InputStream inputStream = url.openStream()) {
            Map<String, RuleScope> scopes = loadRuleScopesFromStream(inputStream, StandardCharsets.UTF_8);
            ruleScopeMap.putAll(scopes);
            LOGGER.debug("Loaded {} rule scopes from URL {}", scopes.size(), url);
        } catch (Exception e) {
            LOGGER.error("Failed to load rule scopes from URL {}", url, e);
        }
    }

    private Map<String, RuleScope> loadRuleScopesFromStream(InputStream input, Charset charset) {
        Map<String, RuleScope> scopeMap = new HashMap<>();
        try (Reader reader = new InputStreamReader(input, charset)) {
            XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
            xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            // Enable namespace awareness to correctly handle files with default or prefixed namespaces on <rules> or children
            xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            // just so it won't try to load DTD in if there's DOCTYPE
            xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
            XMLEventReader xmlReader = xmlFactory.createXMLEventReader(reader);

            parseRuleScopesOnly(scopeMap, xmlReader);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load rule scopes from XML", e);
        }
        return scopeMap;
    }

    private static void parseRuleScopesOnly(Map<String, RuleScope> scopeMap, XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String elementName = element.getName().getLocalPart();
                if (ELEMENT_RULE.equalsIgnoreCase(elementName)) {
                    processRuleScopeOnly(scopeMap, element, reader);
                }
            }
        }
    }

    private static void processRuleScopeOnly(Map<String, RuleScope> scopeMap, StartElement ruleElement, XMLEventReader reader) throws XMLStreamException {
        String key = null;
        String name = null;
        List<String> tags = new ArrayList<>();

        // Support legacy format: <rule key="..." priority="...">
        QName qn = new QName("key");
        Attribute keyAttr = ruleElement.getAttributeByName(qn);
        if (keyAttr != null && keyAttr.getValue() != null && !keyAttr.getValue().isEmpty()) {
            key = keyAttr.getValue().trim();
        }

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isEndElement() && ELEMENT_RULE.equalsIgnoreCase(event.asEndElement().getName().getLocalPart())) {
                break;
            }
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String elementName = element.getName().getLocalPart();

                if ("key".equalsIgnoreCase(elementName)) {
                    String text = reader.getElementText();
                    key = text;
                } else if ("name".equalsIgnoreCase(elementName) || "title".equalsIgnoreCase(elementName)) {
                    String text = reader.getElementText();
                    name = text;
                } else if ("tag".equalsIgnoreCase(elementName)) {
                    String text = reader.getElementText();
                    if (text != null && !text.isEmpty()) {
                        tags.add(text);
                    }
                } else {
                    // Unhandled element like <description> may contain nested elements: skip safely
                    skipElement(reader, elementName);
                }
            }
        }

        if (key != null) {
            RuleScope scope = RulesDefinitionXmlLoader.determineScope(name, tags);
            scopeMap.put(key, scope);
        }
    }

    // Skips over the current element, consuming all nested content until its matching end tag
    private static void skipElement(XMLEventReader reader, String elementName) throws XMLStreamException {
        int depth = 0;
        while (reader.hasNext()) {
            XMLEvent e = reader.nextEvent();
            if (e.isStartElement()) {
                depth++;
            } else if (e.isEndElement()) {
                if (depth == 0 && e.asEndElement().getName().getLocalPart().equalsIgnoreCase(elementName)) {
                    return;
                }
                if (depth > 0) {
                    depth--;
                }
            }
        }
    }
}

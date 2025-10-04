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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry that stores the scope (MAIN, TEST, ALL) for each PMD rule by parsing the XML rule definitions.
 * This allows accurate scope determination during batch analysis without relying on heuristics.
 * Uses the same scope determination logic as {@link RulesDefinitionXmlLoader}.
 */
public class PmdRuleScopeRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PmdRuleScopeRegistry.class);
    private static final String ELEMENT_RULE = "rule";

    private final Map<String, RuleScope> ruleScopeMap = new HashMap<>();

    /**
     * Creates a registry and loads rule scopes from the given XML resource paths.
     *
     * @param xmlResourcePaths Paths to XML rule definition files (e.g., "/org/sonar/plugins/pmd/rules-java.xml")
     */
    public PmdRuleScopeRegistry(String... xmlResourcePaths) {
        for (String path : xmlResourcePaths) {
            loadRulesFromXml(path);
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

    private Map<String, RuleScope> loadRuleScopesFromStream(InputStream input, Charset charset) {
        Map<String, RuleScope> scopeMap = new HashMap<>();
        try (Reader reader = new InputStreamReader(input, charset)) {
            XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
            xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
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
                if (ELEMENT_RULE.equals(elementName)) {
                    processRuleScopeOnly(scopeMap, reader);
                }
            }
        }
    }

    private static void processRuleScopeOnly(Map<String, RuleScope> scopeMap, XMLEventReader reader) throws XMLStreamException {
        String key = null;
        String name = null;
        List<String> tags = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isEndElement() && ELEMENT_RULE.equals(event.asEndElement().getName().getLocalPart())) {
                break;
            }
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String elementName = element.getName().getLocalPart();
                String text = getElementText(reader);

                if ("key".equals(elementName)) {
                    key = text;
                } else if ("name".equals(elementName)) {
                    name = text;
                } else if ("tag".equals(elementName)) {
                    tags.add(text);
                }
            }
        }

        if (key != null && name != null) {
            RuleScope scope = RulesDefinitionXmlLoader.determineScope(name, tags);
            scopeMap.put(key, scope);
        }
    }

    private static String getElementText(XMLEventReader reader) throws XMLStreamException {
        StringBuilder text = new StringBuilder();
        while (reader.hasNext()) {
            XMLEvent event = reader.peek();
            if (event.isEndElement()) {
                break;
            }
            event = reader.nextEvent();
            if (event.isCharacters()) {
                text.append(event.asCharacters().getData());
            }
        }
        return text.toString().trim();
    }
}

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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.pmd.xml.PmdProperty;
import org.sonar.plugins.pmd.xml.PmdRule;
import org.sonar.plugins.pmd.xml.PmdRuleSet;

import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Factory class to create {@link org.sonar.plugins.pmd.xml.PmdRuleSet} out of XML.
 */
public class XmlRuleSetFactory implements RuleSetFactory {

    private static final Logger LOG = Loggers.get(XmlRuleSetFactory.class);
    private static final String INVALID_INPUT = "The PMD configuration file is not valid";

    private final Reader source;
    private final ValidationMessages messages;

    public XmlRuleSetFactory(Reader source, ValidationMessages messages) {
        this.source = source;
        this.messages = messages;
    }


    private List<Element> getChildren(Element parent, String childName, @Nullable Namespace namespace) {
        if (namespace == null) {
            return parent.getChildren(childName);
        } else {
            return parent.getChildren(childName, namespace);
        }
    }

    private Element getChild(Element parent, @Nullable Namespace namespace) {
        final List<Element> children = getChildren(parent, "description", namespace);

        return (children != null && !children.isEmpty()) ? children.get(0) : null;
    }

    private void parsePmdProperties(Element eltRule, PmdRule pmdRule, @Nullable Namespace namespace) {
        for (Element eltProperties : getChildren(eltRule, "properties", namespace)) {
            for (Element eltProperty : getChildren(eltProperties, "property", namespace)) {
                pmdRule.addProperty(new PmdProperty(eltProperty.getAttributeValue("name"), eltProperty.getAttributeValue("value")));
            }
        }
    }

    private void parsePmdPriority(Element eltRule, PmdRule pmdRule, @Nullable Namespace namespace) {
        for (Element eltPriority : getChildren(eltRule, "priority", namespace)) {
            pmdRule.setPriority(Integer.valueOf(eltPriority.getValue()));
        }
    }

    /**
     * Closes all resources.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        source.close();
    }

    /**
     * Parses the given Reader for PmdRuleSets.
     *
     * @return The extracted PmdRuleSet - empty in case of problems, never null.
     */
    @Override
    public PmdRuleSet create() {
        final SAXBuilder builder = new SAXBuilder();
        // prevent XXE attacks
        builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        final Document dom;
        try {
            dom = builder.build(source);
        } catch (JDOMException | IOException e) {
            if (messages != null) {
                messages.addErrorText(INVALID_INPUT + " : " + e.getMessage());
            }
            LOG.error(INVALID_INPUT, e);
            return new PmdRuleSet();
        }

        final Element eltResultset = dom.getRootElement();
        final Namespace namespace = eltResultset.getNamespace();
        final PmdRuleSet result = new PmdRuleSet();

        final String name = eltResultset.getAttributeValue("name");
        final Element descriptionElement = getChild(eltResultset, namespace);

        result.setName(name);

        if (descriptionElement != null) {
            result.setDescription(descriptionElement.getValue());
        }

        for (Element eltRule : getChildren(eltResultset, "rule", namespace)) {
            PmdRule pmdRule = new PmdRule(eltRule.getAttributeValue("ref"));
            pmdRule.setClazz(eltRule.getAttributeValue("class"));
            pmdRule.setName(eltRule.getAttributeValue("name"));
            pmdRule.setMessage(eltRule.getAttributeValue("message"));
            parsePmdPriority(eltRule, pmdRule, namespace);
            parsePmdProperties(eltRule, pmdRule, namespace);
            result.addRule(pmdRule);
        }
        return result;
    }
}

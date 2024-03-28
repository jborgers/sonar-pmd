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
package org.sonar.plugins.pmd.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class PmdRuleSet {

    private String name;
    private String description;

    private List<PmdRule> rules = new ArrayList<>();

    public List<PmdRule> getPmdRules() {
        return rules;
    }

    public void setRules(List<PmdRule> rules) {
        this.rules = rules;
    }

    public void addRule(PmdRule rule) {
        rules.add(rule);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Serializes this RuleSet in an XML document.
     *
     * @param destination The writer to which the XML document shall be written.
     */
    public void writeTo(Writer destination) {
        Element eltRuleset = new Element("ruleset");
        addAttribute(eltRuleset, "name", name);
        addChild(eltRuleset, "description", description);
        for (PmdRule pmdRule : rules) {
            Element eltRule = new Element("rule");
            addAttribute(eltRule, "ref", pmdRule.getRef());
            addAttribute(eltRule, "class", pmdRule.getClazz());
            addAttribute(eltRule, "message", pmdRule.getMessage());
            addAttribute(eltRule, "name", pmdRule.getName());
            addAttribute(eltRule, "language", pmdRule.getLanguage());
            addChild(eltRule, "priority", String.valueOf(pmdRule.getPriority()));
            if (pmdRule.hasProperties()) {
                Element ruleProperties = processRuleProperties(pmdRule);
                if (ruleProperties.getContentSize() > 0) {
                    eltRule.addContent(ruleProperties);
                }
            }
            eltRuleset.addContent(eltRule);
        }
        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        try {
            serializer.output(new Document(eltRuleset), destination);
        } catch (IOException e) {
            throw new IllegalStateException("An exception occurred while serializing PmdRuleSet.", e);
        }
    }

    private void addChild(Element elt, String name, @Nullable String text) {
        if (text != null) {
            elt.addContent(new Element(name).setText(text));
        }
    }

    private void addAttribute(Element elt, String name, @Nullable String value) {
        if (value != null) {
            elt.setAttribute(name, value);
        }
    }

    private Element processRuleProperties(PmdRule pmdRule) {
        Element eltProperties = new Element("properties");
        for (PmdProperty prop : pmdRule.getProperties()) {
            if (isPropertyValueNotEmpty(prop)) {
                Element eltProperty = new Element("property");
                eltProperty.setAttribute("name", prop.getName());
                if (prop.isCdataValue()) {
                    Element eltValue = new Element("value");
                    eltValue.addContent(new CDATA(prop.getCdataValue()));
                    eltProperty.addContent(eltValue);
                } else {
                    eltProperty.setAttribute("value", prop.getValue());
                }
                eltProperties.addContent(eltProperty);
            }
        }
        return eltProperties;
    }

    private boolean isPropertyValueNotEmpty(PmdProperty prop) {
        if (prop.isCdataValue()) {
            return StringUtils.isNotEmpty(prop.getCdataValue());
        }
        return StringUtils.isNotEmpty(prop.getValue());
    }
}

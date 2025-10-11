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

    public void writeTo(Writer destination) {
        org.jdom2.Namespace ns = org.jdom2.Namespace.getNamespace("http://pmd.sourceforge.net/ruleset/2.0.0");
        org.jdom2.Namespace xsi = org.jdom2.Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        Element eltRuleset = new Element("ruleset", ns);
        eltRuleset.addNamespaceDeclaration(xsi);
        eltRuleset.setAttribute("schemaLocation",
            "http://pmd.sourceforge.net/ruleset/2.0.0 http://pmd.sourceforge.net/ruleset_2_0_0.xsd",
            xsi);

        addAttribute(eltRuleset, "name", name);
        addChild(eltRuleset, "description", description, ns);
        for (PmdRule pmdRule : rules) {
            Element eltRule = new Element("rule", ns);
            addAttribute(eltRule, "ref", pmdRule.getRef());
            addAttribute(eltRule, "class", pmdRule.getClazz());
            addAttribute(eltRule, "message", pmdRule.getMessage());
            addAttribute(eltRule, "name", pmdRule.getName());
            addAttribute(eltRule, "language", pmdRule.getLanguage());
            if (pmdRule.getPriority() != null) {
                addChild(eltRule, "priority", String.valueOf(pmdRule.getPriority()), ns);
            }
            if (pmdRule.hasProperties()) {
                Element ruleProperties = processRuleProperties(pmdRule, ns);
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

    private void addChild(Element elt, String name, @Nullable String text, org.jdom2.Namespace ns) {
        if (text != null) {
            elt.addContent(new Element(name, ns).setText(text));
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

    private Element processRuleProperties(PmdRule pmdRule, org.jdom2.Namespace ns) {
        Element eltProperties = new Element("properties", ns);
        for (PmdProperty prop : pmdRule.getProperties()) {
            if (isPropertyValueNotEmpty(prop)) {
                Element eltProperty = new Element("property", ns);
                eltProperty.setAttribute("name", prop.getName());
                if (prop.isCdataValue()) {
                    Element eltValue = new Element("value", ns);
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

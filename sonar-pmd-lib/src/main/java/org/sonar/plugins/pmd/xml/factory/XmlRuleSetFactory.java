/*
 * Moved to lib to share between plugins.
 */
package org.sonar.plugins.pmd.xml.factory;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.pmd.xml.PmdProperty;
import org.sonar.plugins.pmd.xml.PmdRule;
import org.sonar.plugins.pmd.xml.PmdRuleSet;

import org.jetbrains.annotations.Nullable;
import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class XmlRuleSetFactory implements RuleSetFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XmlRuleSetFactory.class);
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

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public PmdRuleSet create() {
        final SAXBuilder builder = new SAXBuilder();
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

        for (Element eltRule : getChildren(eltResultset, "org/sonar/plugins/pmd/rule", namespace)) {
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

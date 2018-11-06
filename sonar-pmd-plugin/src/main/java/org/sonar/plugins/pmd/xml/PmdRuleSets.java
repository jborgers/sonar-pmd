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

package org.sonar.plugins.pmd.xml;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import javax.annotation.Nullable;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Creates {@link PmdRuleSet} from classpath resources.
 */
public class PmdRuleSets implements Closeable {

    private static final Logger LOG = Loggers.get(PmdRuleSets.class);
    private static final String INVALID_INPUT = "The PMD configuration file is not valid";
    private final Reader reader;

    public PmdRuleSets(Reader configReader) {
        this.reader = configReader;
    }

    /**
     * Parses the given ConfigReader for PmdRuleSets.
     *
     * @return The extracted PmdRuleSet.
     * @throws JDOMException May throw exceptions on illegal XML input.
     * @throws IOException   May throw exceptions when problems occur while reading the content.
     */
    public PmdRuleSet parse() throws JDOMException, IOException {
        final SAXBuilder parser = new SAXBuilder();
        final Document dom = parser.build(reader);
        final Element eltResultset = dom.getRootElement();
        final Namespace namespace = eltResultset.getNamespace();
        final PmdRuleSet result = new PmdRuleSet();

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

    /**
     * Convenience method that parses the given InputStream while handling exceptions and closing resources.
     *
     * @param configReader A character stream containing the data of the {@link PmdRuleSet}.
     * @param messages     SonarQube validation messages - allow to inform the enduser about processing problems.
     * @return An instance of PmdRuleSet. The output may be empty but never null.
     */
    public static PmdRuleSet parse(Reader configReader, ValidationMessages messages) {
        try (PmdRuleSets parser = new PmdRuleSets(configReader)) {
            return parser.parse();
        } catch (Exception e) {
            messages.addErrorText(INVALID_INPUT + " : " + e.getMessage());
            LOG.error(INVALID_INPUT, e);
            return new PmdRuleSet();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Element> getChildren(Element parent, String childName, @Nullable Namespace namespace) {
        if (namespace == null) {
            return parent.getChildren(childName);
        } else {
            return parent.getChildren(childName, namespace);
        }
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
            pmdRule.setPriority(eltPriority.getValue());
        }
    }

    /**
     * Closes all resources.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }
}

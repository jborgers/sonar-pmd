/*
 * Java :: IT :: Plugins :: PMD Extension Plugin
 * Copyright (C) 2013-2018 SonarSource SA
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
package org.sonar.examples.pmd;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;

public class PmdExtensionRepository extends RuleRepository {

    // Must be the same than the PMD plugin
    private static final String REPOSITORY_KEY = "pmd";
    private XMLRuleParser ruleParser;

    public PmdExtensionRepository(XMLRuleParser ruleParser) {
        // FIXME: Usage of PmdConstants possible?
        super(REPOSITORY_KEY, "java");
        this.ruleParser = ruleParser;
    }

    @Override
    public List<Rule> createRules() {
        // In this example, new rules are declared in a XML file
        InputStream input = getClass().getResourceAsStream("/org/sonar/examples/pmd/extensions.xml");
        try {
            return ruleParser.parse(input);

        } finally {
            // FIXME Get rid of double close
            try {
                input.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to close stream.", e);
            }
        }
    }
}

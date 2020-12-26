/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2019 SonarSource SA
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

package org.sonar.plugins.pmd.rule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.sonar.api.server.rule.RulesDefinition;

/**
 * Reads the corresponding classpath resource to add HTML descriptions to a given rule.
 * Taken from <code>sslr-squid-bridge:org.sonar.squidbridge.rules.ExternalDescriptionLoader</code>.
 */
public class ExternalDescriptionLoader {

    private final String resourceBasePath;

    public ExternalDescriptionLoader(String resourceBasePath) {
        this.resourceBasePath = resourceBasePath;
    }

    public static void loadHtmlDescriptions(RulesDefinition.NewRepository repository, String languageKey) {
        ExternalDescriptionLoader loader = new ExternalDescriptionLoader(languageKey);
        for (RulesDefinition.NewRule newRule : repository.rules()) {
            loader.addHtmlDescription(newRule);
        }
    }

    public void addHtmlDescription(RulesDefinition.NewRule rule) {
        URL resource = ExternalDescriptionLoader.class.getResource(resourceBasePath + "/" + rule.key() + ".html");
        if (resource != null) {
            addHtmlDescription(rule, resource);
        }
    }

    void addHtmlDescription(RulesDefinition.NewRule rule, URL resource) {
        final StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
            reader
                    .lines()
                    .forEach(l -> {
                        builder.append(l);
                        builder.append(System.lineSeparator());
                    });
            rule.setHtmlDescription(builder.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read: " + resource, e);
        }
    }
}
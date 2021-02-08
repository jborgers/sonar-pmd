/*
 * SonarQube PMD Plugin Integration Test
 * Copyright (C) 2013-2021 SonarSource SA and others
 * mailto:jens AT gerdes DOT digital
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
import java.nio.charset.StandardCharsets;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class PmdExtensionRepository implements RulesDefinition {

    private static final Logger LOGGER = Loggers.get(PmdExtensionRepository.class);

    // Must be the same than the PMD plugin
    private static final String REPOSITORY_KEY = "pmd";
    private static final String LANGUAGE_KEY = "java";

    @Override
    public void define(Context context) {
        NewRepository repository = context.createRepository(REPOSITORY_KEY, LANGUAGE_KEY);

        try (InputStream inputStream = PmdExtensionRepository.class.getResourceAsStream("/org/sonar/examples/pmd/extensions.xml")) {
            new RulesDefinitionXmlLoader()
                    .load(
                            repository,
                            inputStream,
                            StandardCharsets.UTF_8
                    );
        } catch (IOException e) {
            LOGGER.error("Failed to load PMD RuleSet.", e);
        }

        repository.done();
    }
}

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

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.pmd.PmdConstants;
import org.sonar.squidbridge.rules.SqaleXmlLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class PmdApexRulesDefinition implements RulesDefinition {

    private static final Logger LOGGER = Loggers.get(PmdApexRulesDefinition.class);

    public PmdApexRulesDefinition() {
        // do nothing
    }

    static void extractRulesData(NewRepository repository, String xmlRulesFilePath, String htmlDescriptionFolder) {
        try (InputStream inputStream = PmdApexRulesDefinition.class.getResourceAsStream(xmlRulesFilePath)) {
            if (inputStream == null) {
                LOGGER.error("Cannot read {}", xmlRulesFilePath);
            }
            else {
                new RulesDefinitionXmlLoader()
                    .load(
                        repository,
                        inputStream,
                        StandardCharsets.UTF_8
                    );
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load PMD RuleSet.", e);
        }

        ExternalDescriptionLoader.loadHtmlDescriptions(repository, htmlDescriptionFolder);
        SqaleXmlLoader.load(repository, "/com/sonar/sqale/pmd-model-apex.xml");
    }

    @Override
    public void define(Context context) {
        NewRepository repository = context
                .createRepository(PmdConstants.MAIN_APEX_REPOSITORY_KEY, PmdConstants.LANGUAGE_APEX_KEY)
                .setName(PmdConstants.REPOSITORY_APEX_NAME);

        extractRulesData(repository, "/org/sonar/plugins/pmd/rules-apex.xml", "/org/sonar/l10n/pmd/rules/pmd-apex");

        repository.done();
    }

}
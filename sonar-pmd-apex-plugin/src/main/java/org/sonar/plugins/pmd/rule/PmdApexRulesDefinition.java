/*
 * SonarQube PMD7 Plugin - Apex module
 */
package org.sonar.plugins.pmd.rule;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.pmd.PmdConstants;

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
        org.sonar.squidbridge.rules.SqaleXmlLoader.load(repository, "/com/sonar/sqale/pmd-model-apex.xml");
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

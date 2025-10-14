/*
 * SonarQube PMD7 Plugin - Apex module
 */
package org.sonar.plugins.pmd.languages;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.pmd.PmdConstants;

import java.util.List;

public final class ApexLanguageProperties {

    public static final String FILE_SUFFIXES_KEY = "sonar.apex.file.suffixes";
    public static final String DEFAULT_FILE_SUFFIXES = ".cls,.trigger";

    private ApexLanguageProperties() {
        // static utility
    }

    public static List<PropertyDefinition> getProperties() {
        return List.of(
                PropertyDefinition.builder(FILE_SUFFIXES_KEY)
                        .defaultValue(DEFAULT_FILE_SUFFIXES)
                        .name("File Suffixes")
                        .description("Comma-separated list of suffixes for files to analyze.")
                        .category(PmdConstants.PLUGIN_NAME)
                        .subCategory("Apex")
                        .build()
        );
    }
}

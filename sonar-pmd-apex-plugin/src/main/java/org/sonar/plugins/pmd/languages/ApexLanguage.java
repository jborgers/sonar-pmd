/*
 * SonarQube PMD7 Plugin - Apex module
 */
package org.sonar.plugins.pmd.languages;

import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.pmd.PmdConstants;

@ServerSide
public final class ApexLanguage extends AbstractLanguage {

    private final Configuration config;

    public ApexLanguage(Configuration config) {
        super(PmdConstants.LANGUAGE_APEX_KEY, PmdConstants.LANGUAGE_APEX_NAME);
        this.config = config;
    }

    @Override
    public String[] getFileSuffixes() {
        String[] suffixes = config.getStringArray(ApexLanguageProperties.FILE_SUFFIXES_KEY);
        if (suffixes == null || suffixes.length == 0) {
            suffixes = ApexLanguageProperties.DEFAULT_FILE_SUFFIXES.split(",");
        }
        return suffixes;
    }
}

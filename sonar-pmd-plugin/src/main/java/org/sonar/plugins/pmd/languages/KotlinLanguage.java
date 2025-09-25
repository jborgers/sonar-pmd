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
package org.sonar.plugins.pmd.languages;

import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.pmd.PmdConstants;

/**
 * This class defines the Kotlin language.
 */
@ServerSide
public final class KotlinLanguage extends AbstractLanguage {

    private final Configuration config;

    /**
     * Creates a new Kotlin language instance.
     * @param config The SonarQube configuration
     */
    public KotlinLanguage(Configuration config) {
        super(PmdConstants.LANGUAGE_KOTLIN_KEY, PmdConstants.LANGUAGE_KOTLIN_NAME);
        this.config = config;
    }

    @Override
    public String[] getFileSuffixes() {
        String[] suffixes = config.getStringArray(KotlinLanguageProperties.FILE_SUFFIXES_KEY);
        if (suffixes == null || suffixes.length == 0) {
            suffixes = KotlinLanguageProperties.DEFAULT_FILE_SUFFIXES.split(",");
        }
        return suffixes;
    }
}

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

import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.pmd.PmdConstants;

import java.util.List;

/**
 * Properties for the Apex language.
 */
public class ApexLanguageProperties {

    public static final String FILE_SUFFIXES_KEY = "sonar.apex.file.suffixes";
    public static final String DEFAULT_FILE_SUFFIXES = ".cls,.trigger";

    private ApexLanguageProperties() {
        // only statics
    }

    /**
     * Creates a list of property definitions for Apex language.
     * @return List of property definitions
     */
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

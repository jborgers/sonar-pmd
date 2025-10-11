/*
 * SonarQube PMD7 Plugin - Apex module
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
package org.sonar.plugins.pmd;

import org.sonar.api.Plugin;

/**
 * Entry point for the PMD Apex plugin. Initially empty; Apex-specific components
 * will be moved here from the main plugin in subsequent steps.
 */
public class PmdApexPlugin implements Plugin {

    @Override
    public void define(Context context) {
        context.addExtensions(
                org.sonar.api.config.PropertyDefinition.builder(PmdConfiguration.PROPERTY_GENERATE_XML)
                        .defaultValue("false")
                        .name("Generate XML Report")
                        .hidden()
                        .build(),
                PmdApexSensor.class,
                PmdConfiguration.class,
                PmdApexExecutor.class,
                org.sonar.plugins.pmd.rule.PmdApexRulesDefinition.class,
                org.sonar.plugins.pmd.profile.PmdApexSonarWayProfile.class,
                org.sonar.plugins.pmd.languages.ApexLanguage.class,
                PmdViolationRecorder.class
        );

        context.addExtensions(org.sonar.plugins.pmd.languages.ApexLanguageProperties.getProperties());
    }
}

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
package org.sonar.plugins.pmd;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.pmd.languages.ApexLanguage;
import org.sonar.plugins.pmd.languages.ApexLanguageProperties;
import org.sonar.plugins.pmd.profile.PmdApexSonarWayProfile;
import org.sonar.plugins.pmd.rule.PmdApexRulesDefinition;
import org.sonar.plugins.pmd.rule.PmdKotlinRulesDefinition;
import org.sonar.plugins.pmd.rule.PmdRulesDefinition;

/**
 * The {@link PmdPlugin} is the main entry-point of Sonar-PMD.
 */
public class PmdPlugin implements Plugin {

    @Override
    public void define(Context context) {
        context.addExtensions(
                PropertyDefinition.builder(PmdConfiguration.PROPERTY_GENERATE_XML)
                        .defaultValue("false")
                        .name("Generate XML Report")
                        .hidden()
                        .build(),
                PmdSensor.class,
                PmdConfiguration.class,
                PmdJavaExecutor.class,
                PmdKotlinExecutor.class,
                PmdApexExecutor.class,
                PmdRulesDefinition.class,
                PmdKotlinRulesDefinition.class,
                PmdApexRulesDefinition.class,
                ApexLanguage.class,
                PmdApexSonarWayProfile.class,
                PmdViolationRecorder.class,
                DefaultClasspathProvider.class
        );

        context.addExtensions(ApexLanguageProperties.getProperties());
    }
}

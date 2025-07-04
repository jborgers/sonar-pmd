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
package org.sonar.plugins.pmd.profile;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.plugins.pmd.PmdConstants;
import org.sonar.plugins.pmd.rule.PmdApexRulesDefinition;

/**
 * Defines a built-in quality profile for Apex language.
 */
public class PmdApexSonarWayProfile implements BuiltInQualityProfilesDefinition {

    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("Sonar way", PmdConstants.LANGUAGE_APEX_KEY);
        profile.setDefault(true);

        // Add a few rules to the profile
        // These are just examples, you can add more rules as needed
        profile.activateRule(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "AvoidDebugStatements");
        profile.activateRule(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "OperationWithLimitsInLoop"); // Covers both DML and SOQL in loops
        profile.activateRule(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "AvoidGlobalModifier");
        profile.activateRule(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "AvoidLogicInTrigger");
        profile.activateRule(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "AvoidNonExistentAnnotations");
        profile.activateRule(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "ClassNamingConventions");
        profile.activateRule(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "MethodNamingConventions");
        profile.activateRule(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "PropertyNamingConventions");
        profile.activateRule(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "LocalVariableNamingConventions");

        profile.done();
    }
}

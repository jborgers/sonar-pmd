/*
 * SonarQube PMD7 Plugin - Apex module
 */
package org.sonar.plugins.pmd.profile;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.plugins.pmd.PmdConstants;

public class PmdApexSonarWayProfile implements BuiltInQualityProfilesDefinition {

    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("Sonar way", PmdConstants.LANGUAGE_APEX_KEY);
        profile.setDefault(true);

        profile.activateRule(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "AvoidDebugStatements");
        profile.activateRule(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "OperationWithLimitsInLoop");
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

/*
 * SonarQube PMD Plugin Integration Test
 * Copyright (C) 2013-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package com.sonar.it.java.suite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.MavenBuild;
import com.sonar.orchestrator.locator.MavenLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;

import static com.sonar.orchestrator.locator.FileLocation.byWildcardMavenFilename;
import static com.sonar.orchestrator.locator.FileLocation.ofClasspath;
import static org.assertj.core.api.Assertions.assertThat;

class PmdIT {

    private static final String SONAR_JAVA_PLUGIN_VERSION_KEY = "test.sonar.plugin.version.java";
    private static final String SONAR_VERSION_KEY = "test.sonar.version";

    private static final Orchestrator ORCHESTRATOR = Orchestrator
                .builderEnv()
                .setSonarVersion(determineSonarqubeVersion())
                .addPlugin(MavenLocation.create(
                        "org.sonarsource.java",
                        "sonar-java-plugin",
                        determineJavaPluginVersion()
                ))
                .addPlugin(byWildcardMavenFilename(new File("../sonar-pmd-plugin/target"), "sonar-pmd-plugin-*.jar"))
                .addPlugin(byWildcardMavenFilename(new File("./target"), "integration-test-*.jar"))
                .restoreProfileAtStartup(ofClasspath("/com/sonar/it/java/PmdTest/pmd-junit-rules.xml"))
                .restoreProfileAtStartup(ofClasspath("/com/sonar/it/java/PmdTest/pmd-extensions-profile.xml"))
                .restoreProfileAtStartup(ofClasspath("/com/sonar/it/java/PmdTest/pmd-backup.xml"))
                .build();

    @BeforeAll
    static void startSonar() {
        ORCHESTRATOR.start();
    }

    @AfterEach
    void resetData() {
        ORCHESTRATOR.resetData();
    }

    private static String determineJavaPluginVersion() {
        return System.getProperty(SONAR_JAVA_PLUGIN_VERSION_KEY, "DEV");
    }

    private static String determineSonarqubeVersion() {
        return System.getProperty(SONAR_VERSION_KEY, "LATEST_RELEASE[6.7]");
    }

    private static String keyFor(String projectKey, String srcDir, String pkgDir, String cls) {
        return "com.sonarsource.it.projects:" + projectKey + ":" + srcDir + pkgDir + cls;
    }

    private static String keyFor(String projectKey, String pkgDir, String cls) {
        return keyFor(projectKey, "src/main/java/", pkgDir, cls + ".java");
    }

    private static String keyForTest() {
        return keyFor("pmd-junit-rules", "src/test/java/", "", "ProductionCodeTest" + ".java");
    }

    @Test
    void pmdExtensions() {
        MavenBuild build = MavenBuild.create(TestUtils.projectPom("pmd-extensions"))
                .setCleanSonarGoals()
                .setProperty("sonar.java.binaries", ".")
                .setProperty("sonar.profile", "pmd-extensions");
        final BuildResult buildResult = ORCHESTRATOR.executeBuild(build);
        final String log = buildResult.getLogs();

        assertThat(log).contains("Start MaximumMethodsCountCheck");
        assertThat(log).contains("End MaximumMethodsCountCheck");

        List<Issue> issues = retrieveIssues(keyFor("pmd-extensions", "pmd/", "Errors"));
        assertThat(issues).hasSize(3);
        List<String> messages = new ArrayList<>();
        for (Issue issue : issues) {
            messages.add(issue.message());
        }
        assertThat(messages).containsOnly(
                "Avoid too many methods",
                "A catch statement should never catch throwable since it includes errors.",
                "Avoid if without using brace");
    }

    /**
     * SONAR-3346
     */
    @Test
    void ruleAvoidDuplicateLiterals() {
        MavenBuild build = MavenBuild.create(TestUtils.projectPom("pmd-avoid-duplicate-literals"))
                .setCleanSonarGoals()
                .setProperty("sonar.profile", "pmd");
        ORCHESTRATOR.executeBuild(build);

        List<Issue> issues = retrieveIssues(IssueQuery.create()
                .rules("pmd:AvoidDuplicateLiterals")
                .components(keyFor("pmd-avoid-duplicate-literals", "", "AvoidDuplicateLiterals")));
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).message()).contains("appears 5 times in this file");
    }

    /**
     * SONAR-1076
     */
    @Test
    void junitRules() {
        MavenBuild build = MavenBuild.create(TestUtils.projectPom("pmd-junit-rules"))
                .setCleanSonarGoals()
                .setProperty("sonar.profile", "pmd-junit");
        ORCHESTRATOR.executeBuild(build);

        List<Issue> testIssues = retrieveIssues(keyForTest());
        assertThat(testIssues).hasSize(1);
        assertThat(testIssues.get(0).message()).matches("This class name ends with '?Test'? but contains no test cases");
        assertThat(testIssues.get(0).ruleKey()).isEqualTo("pmd-unit-tests:TestClassWithoutTestCases");

        List<Issue> prodIssues = retrieveIssues(keyFor("pmd-junit-rules", "", "ProductionCode"));
        assertThat(prodIssues).hasSize(1);
        assertThat(prodIssues.get(0).message()).contains("Avoid unused private fields such as 'unused'.");
        assertThat(prodIssues.get(0).ruleKey()).isEqualTo("pmd:UnusedPrivateField");
    }

    /**
     * SONARPLUGINS-3318
     */
    @Test
    void pmd_should_have_access_to_external_libraries_in_its_classpath() {
        MavenBuild build = MavenBuild.create(TestUtils.projectPom("pmd-extensions"))
                .setCleanPackageSonarGoals()
                .setProperty("sonar.profile", "pmd-extensions");
        ORCHESTRATOR.executeBuild(build);

        List<Issue> issues = retrieveIssues(keyFor("pmd-extensions", "pmd/", "Bar"));
        assertThat(issues).hasSize(1);
    }

    private List<Issue> retrieveIssues(IssueQuery query) {
        return ORCHESTRATOR.getServer().wsClient().issueClient().find(query).list();
    }

    private List<Issue> retrieveIssues(String componentKey) {
        return retrieveIssues(IssueQuery.create().components(componentKey));
    }
}

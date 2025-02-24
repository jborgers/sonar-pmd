/*
 * SonarQube PMD7 Plugin Integration Test
 * Copyright (C) 2013-2021 SonarSource SA and others
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
package com.sonar.it.java.suite;

import com.sonar.it.java.suite.orchestrator.PmdTestOrchestrator;
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.MavenBuild;
import com.sonar.orchestrator.http.HttpException;
import org.apache.commons.lang3.JavaVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;

import java.util.List;
import java.util.stream.Collectors;

import static com.sonar.it.java.suite.TestUtils.keyFor;
import static org.assertj.core.api.Assertions.assertThat;

class PmdIT {

    private static PmdTestOrchestrator ORCHESTRATOR;

    @BeforeAll
    static void startSonar() {
        ORCHESTRATOR = PmdTestOrchestrator.init();
        ORCHESTRATOR.start();
    }

    @ParameterizedTest
    @EnumSource(value = JavaVersion.class, mode = EnumSource.Mode.INCLUDE, names = {"JAVA_1_8", "JAVA_11", "JAVA_17", "JAVA_20"})
    void testPmdExtensionsWithDifferentJavaVersions(JavaVersion version) {

        // given
        final String projectName = "pmd-extensions";
        final MavenBuild build = MavenBuild
                .create(TestUtils.projectPom(projectName))
                .setCleanSonarGoals()
                .setProperty("maven.compiler.source", version.toString())
                .setProperty("maven.compiler.target", version.toString())
                .setProperty("sonar.java.binaries", ".");

        try {
            ORCHESTRATOR.associateProjectToQualityProfile("pmd-extensions-profile", projectName);

            // when
            final BuildResult buildResult = ORCHESTRATOR.executeBuild(build);

            // then
            final String log = buildResult.getLogs();
            assertThat(log)
                    .contains("Start MaximumMethodsCountCheck")
                    .contains("End MaximumMethodsCountCheck");

            final List<Issue> issues = retrieveIssues(keyFor(projectName, "pmd/", "Errors"));

            final List<String> messages = issues
                    .stream()
                    .map(Issue::message)
                    .collect(Collectors.toList());

            System.out.println("messages: " + messages);

            assertThat(issues)
                    .hasSize(3);



            assertThat(messages)
                    .containsOnly(
                            "Avoid too many methods",
                            "A catch statement should never catch throwable since it includes errors.",
                            "Avoid if without using brace"
                    );
        } catch (HttpException e) {
            System.out.println("Failed to associate Project To Quality Profile: " + e.getMessage() + " body: " + e.getBody());
            throw e;
        } finally {
            // Cleanup
            ORCHESTRATOR.resetData(projectName);
        }
    }

    /**
     * SONAR-3346
     */
    @Test
    void testRuleAvoidDuplicateLiterals() {

        // given
        final String projectName = "pmd-avoid-duplicate-literals";
        final MavenBuild build = MavenBuild
                .create(TestUtils.projectPom(projectName))
                .setCleanSonarGoals();

        try {
            ORCHESTRATOR.associateProjectToQualityProfile("pmd", projectName);

            // when
            ORCHESTRATOR.executeBuild(build);

            // then
            final List<Issue> issues = ORCHESTRATOR.retrieveIssues(
                    IssueQuery.create()
                            .rules("pmd:AvoidDuplicateLiterals")
                            .components(keyFor(projectName, "", "AvoidDuplicateLiterals")
                            )
            );

            assertThat(issues)
                    .hasSize(1);

            assertThat(issues.get(0).message())
                    .contains("appears 5 times in this file");

        } catch (HttpException e) {
            System.out.println("Failed to associate Project To Quality Profile: " + e.getMessage() + " body: " + e.getBody());
            throw e;
        } finally {
            // Cleanup
            ORCHESTRATOR.resetData(projectName);
        }
    }

    /**
     * SONARPLUGINS-3318
     */
    @Test
    void pmdShouldHaveAccessToExternalLibrariesInItsClasspath() {

        // given
        final String projectName = "pmd-extensions";
        final MavenBuild build = MavenBuild
                .create(TestUtils.projectPom(projectName))
                .setCleanPackageSonarGoals();
        try {
            ORCHESTRATOR.associateProjectToQualityProfile("pmd-extensions-profile", projectName);

            // when
            ORCHESTRATOR.executeBuild(build);

            // then
            // PMD7-MIGRATION: added to force one violation in pmdShouldHaveAccessToExternalLibrariesInItsClasspath: is this testing the correct thing?
            final List<Issue> issues = retrieveIssues(keyFor(projectName, "pmd/", "Bar"));
            assertThat(issues)
                    .hasSize(1);

        } catch (HttpException e) {
            System.out.println("Failed to associate Project To Quality Profile: " + e.getMessage() + " body: " + e.getBody());
            throw e;
        } finally {
            // Cleanup
            ORCHESTRATOR.resetData(projectName);
        }
    }

    @Test
    void pmdShouldRunWithAllRulesEnabled() {

        // given
        final String projectName = "pmd-extensions";
        final MavenBuild build = MavenBuild
                .create(TestUtils.projectPom(projectName))
                .setCleanPackageSonarGoals();
        try {
            ORCHESTRATOR.associateProjectToQualityProfile("pmd-all-rules", projectName);

            // when
            ORCHESTRATOR.executeBuild(build);

            // then
            final List<Issue> issues = retrieveIssues(keyFor(projectName, "pmd/", "Bar"));
            assertThat(issues)
                    .isNotEmpty();

        } catch (HttpException e) {
            System.out.println("Failed to associate Project To Quality Profile: " + e.getMessage() + " body: " + e.getBody());
            throw e;
        } finally {
            // Cleanup
            ORCHESTRATOR.resetData(projectName);
        }
    }

    private List<Issue> retrieveIssues(String componentKey) {
        final IssueQuery issueQuery = IssueQuery.create();
        issueQuery.urlParams().put("componentKeys", componentKey);
        return ORCHESTRATOR.retrieveIssues(issueQuery);
    }
}

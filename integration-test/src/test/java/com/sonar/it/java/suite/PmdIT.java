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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
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
    @EnumSource(value = DefinedJavaVersion.class, mode = EnumSource.Mode.INCLUDE, names = {"JAVA_1_8", "JAVA_17", "JAVA_21", "JAVA_25", "JAVA_25_PREVIEW"})
    void testPmdExtensionsWithDifferentJavaVersions(DefinedJavaVersion version) {

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

            final List<Issue> issues = retrieveIssues(keyFor(projectName, "src/main/java", "pmd", "Errors", ".java"));

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
     * SONAR-1076
     */
    @Test
    void testJunitRules() {

        // given
        final String projectName = "pmd-junit-rules";
        final MavenBuild build = MavenBuild
                .create(TestUtils.projectPom(projectName))
                .setCleanSonarGoals();

        ORCHESTRATOR.associateProjectToQualityProfile("pmd-test-rule-profile", projectName);

        // when
        ORCHESTRATOR.executeBuild(build);

        // then
        // (component -> com.sonarsource.it.projects:pmd-junit-rules:src/test/java/ProductionCodeTest.java)
        String testComponentKey = keyFor("pmd-junit-rules", "src/test/java", "", "ProductionCodeTest", ".java");
        final List<Issue> testIssues = retrieveIssues(testComponentKey);

        int expectedTestIssuesCount = 2;
        assertThat(testIssues)
                .withFailMessage(printFailedIssueCountCheck(testIssues, expectedTestIssuesCount))
                .hasSize(expectedTestIssuesCount);

        Optional<Issue> testIssue1 = testIssues.stream().filter(i -> i.ruleKey().equals("pmd:UnitTestContainsTooManyAsserts")).findFirst();
        assertThat(testIssue1).withFailMessage("expected sources test issue not found").isPresent();
        assertThat(testIssue1.get().message()).isEqualTo("Unit tests should not contain more than 1 assert(s).");

        Optional<Issue> testIssue2 = testIssues.stream().filter(i -> i.ruleKey().equals("pmd:AvoidIfWithoutBraceTest")).findFirst();
        assertThat(testIssue2).withFailMessage("expected source test only issue not found").isPresent();


        // component -> com.sonarsource.it.projects:pmd-junit-rules:src/main/java/ProductionCode.java
        final List<Issue> prodIssues = retrieveIssues(keyFor(projectName, "src/main/java", "", "ProductionCode", ".java"));

        int expectedProdIssueCount = 2;
        assertThat(prodIssues)
                .withFailMessage(printFailedIssueCountCheck(prodIssues, expectedProdIssueCount))
                .hasSize(expectedProdIssueCount);

        Optional<Issue> prodIssue1 = testIssues.stream().filter(i -> i.ruleKey().equals("pmd:UnusedPrivateField")).findFirst();
        assertThat(prodIssue1).withFailMessage("expected sources main rule not found").isPresent();
        assertThat(prodIssue1.get().message()).contains("Avoid unused private fields such as 'unused'.");

        Optional<Issue> prodIssue2 = testIssues.stream().filter(i -> i.ruleKey().equals("pmd:AvoidIfWithoutBrace")).findFirst();
        assertThat(prodIssue2).withFailMessage("expected sources main only issue not found").isPresent();

        // Cleanup
        ORCHESTRATOR.resetData(projectName);
    }

    private static @NotNull Supplier<String> printFailedIssueCountCheck(List<Issue> prodIssues, int expectedCount) {
        String listOfIssueKeys = prodIssues.stream().map(Issue::ruleKey).collect(Collectors.joining(";"));
        return () -> "Did not find " + expectedCount + " issues, but " + prodIssues.size() + ": " + listOfIssueKeys;
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
            ORCHESTRATOR.associateProjectToQualityProfile("pmd-backup-profile", projectName);

            // when
            ORCHESTRATOR.executeBuild(build);

            // then
            String avoidDuplicateLiteralsKey = keyFor(projectName, "src/main/java", "", "AvoidDuplicateLiterals", ".java");
            final List<Issue> issues = ORCHESTRATOR.retrieveIssues(
                    IssueQuery.create()
                            .rules("pmd:AvoidDuplicateLiterals")
                            .components(avoidDuplicateLiteralsKey)
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
            final List<Issue> issues = retrieveIssues(keyFor(projectName, "src/main/java/", "pmd/", "Errors", ".java"));

            assertThat(issues)
                    .hasSize(3);

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
            ORCHESTRATOR.associateProjectToQualityProfile("pmd-all-rules-profile", projectName);

            // when
            ORCHESTRATOR.executeBuild(build);

            // then
            final List<Issue> issues = retrieveIssues(keyFor(projectName, "src/main/java", "pmd", "Bar", ".java"));

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

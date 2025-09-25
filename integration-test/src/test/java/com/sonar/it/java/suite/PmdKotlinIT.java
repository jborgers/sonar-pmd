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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;

import java.util.List;
import java.util.stream.Collectors;

import static com.sonar.it.java.suite.TestUtils.keyFor;
import static org.assertj.core.api.Assertions.assertThat;

class PmdKotlinIT {

    private static PmdTestOrchestrator orchestrator;

    @BeforeAll
    static void startSonar() {
        orchestrator = PmdTestOrchestrator.init();
        orchestrator.start();
    }

    @Test
    void testKotlinRules() {
        // given
        final String projectName = "pmd-kotlin-rules";
        final String suffix = ".kt";
        final String srcDir = "src/main/kotlin";

        final MavenBuild build = MavenBuild
                .create(TestUtils.projectPom(projectName))
                .setCleanSonarGoals()
                .setProperty("sonar.kotlin.file.suffixes", suffix)
                .setProperty("sonar.sources", srcDir)
                .setProperty("sonar.java.binaries", "target/classes")
                .setProperty("sonar.log.level", "DEBUG")
                .setProperty("sonar.verbose", "true");

        try {
            orchestrator.associateProjectToQualityProfile("pmd-kotlin-profile", projectName, "kotlin");

            // when
            final BuildResult buildResult = orchestrator.executeBuild(build);

            // then
            final String log = buildResult.getLogs();
            assertThat(log).contains("Kotlin");

            System.out.println("[DEBUG_LOG] Build log: " + log);

            // being this specific yields no results... what can be wrong? component -> com.sonarsource.it.projects:pmd-kotlin-rules:src/main/kotlin/com/example/KotlinErrors.kt
            final List<Issue> issues = retrieveIssues(keyFor(projectName, srcDir, "com/example", "KotlinErrors", suffix));

            final List<String> messages = issues
                    .stream()
                    .map(Issue::message)
                    .collect(Collectors.toList());

            assertThat(issues).hasSize(2);

            assertThat(messages)
                    .contains(
                            "Function names should have non-cryptic and clear names.",
                            "Ensure you override both equals() and hashCode()"
                    );
        } catch (HttpException e) {
            System.out.println("Failed to associate Project To Quality Profile: " + e.getMessage() + " body: " + e.getBody());
            throw e;
        } finally {
            // Cleanup
            orchestrator.resetData(projectName);
        }
    }

    @Test
    void pmdKotlinShouldRunWithAllRulesEnabled() {
        // given
        final String projectName = "pmd-kotlin-rules";
        final MavenBuild build = MavenBuild
                .create(TestUtils.projectPom(projectName))
                .setCleanPackageSonarGoals()
                .setProperty("sonar.kotlin.file.suffixes", ".kt")
                .setProperty("sonar.sources", "src/main/kotlin")
                .setProperty("sonar.java.binaries", "target/classes")
                .setProperty("sonar.log.level", "DEBUG")
                .setProperty("sonar.verbose", "true");
        try {
            orchestrator.associateProjectToQualityProfile("pmd-kotlin-all-rules", projectName, "kotlin");

            // when
            final BuildResult buildResult = orchestrator.executeBuild(build);

            // then
            final String log = buildResult.getLogs();
            System.out.println("[DEBUG_LOG] Build log: " + log);

            final List<Issue> issues = retrieveIssues(keyFor(projectName, "src/main/kotlin", "com/example", "KotlinErrors", ".kt"));
            System.out.println("[DEBUG_LOG] Issues found: " + issues.size());

            // Also check for issues on EqualsOnly class specifically
            final List<Issue> equalsOnlyIssues = retrieveIssues(keyFor(projectName, "src/main/kotlin","com/example", "EqualsOnly", ".kt"));
            System.out.println("[DEBUG_LOG] EqualsOnly issues found: " + equalsOnlyIssues.size());

            assertThat(issues).isNotEmpty();

        } catch (HttpException e) {
            System.out.println("Failed to associate Project To Quality Profile: " + e.getMessage() + " body: " + e.getBody());
            throw e;
        } finally {
            // Cleanup
            orchestrator.resetData(projectName);
        }
    }

    private List<Issue> retrieveIssues(String componentKey) {
        final IssueQuery issueQuery = IssueQuery.create();
        issueQuery.urlParams().put("componentKeys", componentKey);
        return orchestrator.retrieveIssues(issueQuery);
    }
}

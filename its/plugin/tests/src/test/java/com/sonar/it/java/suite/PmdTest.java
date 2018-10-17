/*
 * PMD :: IT :: Plugin :: Tests
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

import java.util.ArrayList;
import java.util.List;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.MavenBuild;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;

import static org.fest.assertions.Assertions.assertThat;

public class PmdTest {

  @ClassRule
  public static Orchestrator orchestrator = PmdTestSuite.ORCHESTRATOR;

  private static String keyFor(String projectKey, String srcDir, String pkgDir, String cls) {
    return "com.sonarsource.it.projects:" + projectKey + ":" + srcDir + pkgDir + cls;
  }

  private static String keyFor(String projectKey, String pkgDir, String cls) {
    return keyFor(projectKey, "src/main/java/", pkgDir, cls + ".java");
  }

  private static String keyForTest() {
    return keyFor("pmd-junit-rules", "src/test/java/", "", "ProductionCodeTest" + ".java");
  }

  @Before
  public void resetData() {
    orchestrator.resetData();
  }

  @Test
  public void pmdExtensions() {
    MavenBuild build = MavenBuild.create(TestUtils.projectPom("pmd-extensions"))
      .setCleanSonarGoals()
      .setProperty("sonar.java.binaries", ".")
      .setProperty("sonar.profile", "pmd-extensions");
    final BuildResult buildResult = orchestrator.executeBuild(build);
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
  public void ruleAvoidDuplicateLiterals() {
    MavenBuild build = MavenBuild.create(TestUtils.projectPom("pmd-avoid-duplicate-literals"))
      .setCleanSonarGoals()
      .setProperty("sonar.profile", "pmd");
    orchestrator.executeBuild(build);

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
  public void junitRules() {
    MavenBuild build = MavenBuild.create(TestUtils.projectPom("pmd-junit-rules"))
      .setCleanSonarGoals()
      .setProperty("sonar.profile", "pmd-junit");
    orchestrator.executeBuild(build);

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
  public void pmd_should_have_access_to_external_libraries_in_its_classpath() {
    MavenBuild build = MavenBuild.create(TestUtils.projectPom("pmd-extensions"))
      .setCleanPackageSonarGoals()
      .setProperty("sonar.profile", "pmd-extensions");
    orchestrator.executeBuild(build);

    List<Issue> issues = retrieveIssues(keyFor("pmd-extensions", "pmd/", "Bar"));
    assertThat(issues).hasSize(1);
  }

  private List<Issue> retrieveIssues(IssueQuery query) {
    return orchestrator.getServer().wsClient().issueClient().find(query).list();
  }

  private List<Issue> retrieveIssues(String componentKey) {
    return retrieveIssues(IssueQuery.create().components(componentKey));
  }
}

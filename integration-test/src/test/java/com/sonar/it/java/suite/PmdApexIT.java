/*
 * SonarQube PMD7 Plugin Integration Test - Apex sanity
 */
package com.sonar.it.java.suite;

import com.sonar.it.java.suite.orchestrator.PmdTestOrchestrator;
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.MavenBuild;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;

import java.util.List;

import static com.sonar.it.java.suite.TestUtils.keyFor;
import static org.assertj.core.api.Assertions.assertThat;

class PmdApexIT {

    private static PmdTestOrchestrator ORCHESTRATOR;

    @BeforeAll
    static void startSonar() {
        ORCHESTRATOR = PmdTestOrchestrator.init();
        ORCHESTRATOR.start();
    }

    @Test
    void sanity_apex_project_analyzes_and_reports_issue() {
        // given
        final String projectName = "pmd-apex-rules";
        final MavenBuild build = MavenBuild
                .create(TestUtils.projectPom(projectName))
                .setCleanSonarGoals()
                .setProperty("sonar.sources", "src/main/apex");

        ORCHESTRATOR.associateProjectToQualityProfile("pmd-apex-profile", projectName, "apex");

        // when
        BuildResult result = ORCHESTRATOR.executeBuild(build);

        // then
        String logs = result.getLogs();
        assertThat(logs)
                .as("Sonar logs should mention PMD Apex execution")
                .contains("PMD Apex");

        List<Issue> issues1 = ORCHESTRATOR.retrieveIssues(IssueQuery.create()
                .components(keyFor(projectName, "src/main/apex", "", "BooleanViolation", ".cls")));

        assertThat(issues1)
                .as("Expect at least one Apex issue to be reported for BooleanViolation.cls")
                .isNotEmpty();

        // Preferably, we find BooleanViolation rule
        assertThat(issues1.stream().anyMatch(i -> "pmd-apex:AvoidBooleanMethodParameters".equals(i.ruleKey())))
                .as("Expect BooleanViolation to be reported on BooleanViolation.cls")
                .isTrue();

        List<Issue> issuesCrypto = ORCHESTRATOR.retrieveIssues(IssueQuery.create()
                .components(keyFor(projectName, "src/main/apex", "", "WeakCrypto", ".cls")));

        assertThat(issuesCrypto)
                .as("Expect at least one Apex issue to be reported for WeakCrypto.cls")
                .isNotEmpty();

        // Preferably, we find ApexBadCrypto rule
        assertThat(issuesCrypto.stream().anyMatch(i -> "pmd-apex:ApexBadCrypto".equals(i.ruleKey())))
                .as("Expect ApexBadCrypto to be reported on WeakCrypto.cls")
                .isTrue();

        // cleanup
        ORCHESTRATOR.resetData(projectName);
    }
}

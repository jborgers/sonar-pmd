/*
 * SonarQube PMD7 Plugin Integration Test
 */
package com.sonar.it.java.suite;

import com.sonar.it.java.suite.orchestrator.PmdTestOrchestrator;
import com.sonar.orchestrator.build.MavenBuild;
import com.sonar.orchestrator.build.BuildResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sanity check that our integration test suite can start and run against both
 * the lowest and highest supported SonarQube versions.
 */
class SanitySonarVersionsIT {

    private static final String SONAR_VERSION_KEY = "test.sonar.version";

    @ParameterizedTest(name = "sanity on SonarQube {0}")
    @ValueSource(strings = {
            // Lowest supported SonarQube LTS line
            "LATEST_RELEASE[9.9]",
            // Highest supported SonarQube current line (see README table)
            "LATEST_RELEASE[25.9]"
    })
    void sanity_runs_on_lowest_and_highest_supported_versions(String sonarqubeVersion) {
        final String previous = System.getProperty(SONAR_VERSION_KEY);
        System.setProperty(SONAR_VERSION_KEY, sonarqubeVersion);

        PmdTestOrchestrator orchestrator = null;
        try {
            orchestrator = PmdTestOrchestrator.init();
            orchestrator.start();

            final String projectName = "pmd-extensions";
            final MavenBuild build = MavenBuild
                    .create(TestUtils.projectPom(projectName))
                    .setCleanSonarGoals()
                    // keep analysis minimal for sanity run
                    .setProperty("sonar.java.binaries", ".");

            orchestrator.associateProjectToQualityProfile("pmd-extensions-profile", projectName);
            final BuildResult result = orchestrator.executeBuild(build); // will throw if analysis fails
            assertThat(result.getLogs()).contains("[INFO] Sensor PmdSensor [pmd]");

            // Additionally run a minimal Kotlin project analysis to ensure Kotlin support works
            final String kotlinProject = "pmd-kotlin-rules";
            final MavenBuild kotlinBuild = MavenBuild
                    .create(TestUtils.projectPom(kotlinProject))
                    .setCleanSonarGoals();
            orchestrator.associateProjectToQualityProfile("pmd-kotlin-profile", kotlinProject, "kotlin");
            final BuildResult kotlinResult = orchestrator.executeBuild(kotlinBuild);
            assertThat(kotlinResult.getLogs()).contains("[INFO] Sensor PmdSensor [pmd]");
        }
        finally {
            // restore previous property to not affect other tests
            if (previous != null) {
                System.setProperty(SONAR_VERSION_KEY, previous);
            } else {
                System.clearProperty(SONAR_VERSION_KEY);
            }
            if (orchestrator != null) {
                try {
                    orchestrator.stop();
                } catch (Throwable ignored) {
                    // ignore
                }
            }
        }
    }
}

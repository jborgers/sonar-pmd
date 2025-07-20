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
package com.sonar.it.java.suite.orchestrator;

import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.MavenBuild;
import com.sonar.orchestrator.junit4.OrchestratorRule;
import com.sonar.orchestrator.locator.MavenLocation;
import org.sonar.wsclient.SonarClient;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;

import java.io.File;
import java.util.List;

import static com.sonar.orchestrator.container.Server.ADMIN_LOGIN;
import static com.sonar.orchestrator.container.Server.ADMIN_PASSWORD;
import static com.sonar.orchestrator.locator.FileLocation.byWildcardMavenFilename;
import static com.sonar.orchestrator.locator.FileLocation.ofClasspath;

/**
 * Wraps the {@link com.sonar.orchestrator.Orchestrator} and replaces deprecated methods with a different implementation.
 */
public class PmdTestOrchestrator {

    private static final String SONAR_JAVA_PLUGIN_VERSION_KEY = "test.sonar.plugin.version.java";
    private static final String SONAR_KOTLIN_PLUGIN_VERSION_KEY = "test.sonar.plugin.version.kotlin";
    private static final String SONAR_VERSION_KEY = "test.sonar.version";
    private static final String LANGUAGE_KEY = "java";
    private static final String CENTRAL_MAVEN = "https://repo1.maven.org/maven2";

    static {
        // override jfrog as artifactory because it now doesn't have anonymous login anymore
        // don't rely on configuration file, less dependencies
        // issue see https://community.sonarsource.com/t/orchestrator-adding-support-for-downloading-artifacts-without-jfrog/108867
        if (System.getProperty("orchestrator.artifactory.url") == null) {
            System.setProperty("orchestrator.artifactory.url", CENTRAL_MAVEN);
        }
    }
    private final OrchestratorRule delegate;

    private PmdTestOrchestrator(OrchestratorRule delegate) {
        this.delegate = delegate;
    }

    public void resetData(String project) {
        SonarClient
                .builder()
                .url(delegate.getServer().getUrl())
                .login(ADMIN_LOGIN)
                .password(ADMIN_PASSWORD)
                .connectTimeoutMilliseconds(300_000)
                .readTimeoutMilliseconds(600_000)
                .build()
                .post("/api/projects/delete?project=" + deriveProjectKey(project));
    }

    public void start() {
        delegate.start();
    }

    public BuildResult executeBuild(MavenBuild build) {
        return delegate.executeBuild(build);
    }

    public List<Issue> retrieveIssues(IssueQuery query) {
        return SonarClient.create(delegate.getServer().getUrl())
                .issueClient()
                .find(query)
                .list();
    }

    public void associateProjectToQualityProfile(String profile, String project) {
        associateProjectToQualityProfile(profile, project, LANGUAGE_KEY);
    }

    public void associateProjectToQualityProfile(String profile, String project, String language) {
        final String projectKey = deriveProjectKey(project);
        delegate.getServer().provisionProject(projectKey, project);
        delegate.getServer().associateProjectToQualityProfile(projectKey, language, profile);
    }

    public static PmdTestOrchestrator init() {
        try {
            final OrchestratorRule orchestrator = OrchestratorRule
                    .builderEnv().useDefaultAdminCredentialsForBuilds(true)
                    .setSonarVersion(determineSonarqubeVersion())
                    .addPlugin(MavenLocation.create(
                            "org.sonarsource.java",
                            "sonar-java-plugin",
                            determineJavaPluginVersion()
                    ))
                    .addPlugin(MavenLocation.create(
                            "org.sonarsource.kotlin",
                            "sonar-kotlin-plugin",
                            determineKotlinPluginVersion()
                    ))
                    .addPlugin(byWildcardMavenFilename(new File("../sonar-pmd-plugin/target"), "sonar-pmd-plugin-*.jar"))
                    .restoreProfileAtStartup(ofClasspath("/com/sonar/it/java/PmdTest/pmd-extensions-profile.xml"))
                    .restoreProfileAtStartup(ofClasspath("/com/sonar/it/java/PmdTest/pmd-backup.xml"))
                    .restoreProfileAtStartup(ofClasspath("/com/sonar/it/java/PmdTest/pmd-all-rules.xml"))
                    .restoreProfileAtStartup(ofClasspath("/com/sonar/it/java/PmdTest/pmd-test-rule.xml"))
                    .restoreProfileAtStartup(ofClasspath("/com/sonar/it/java/PmdTest/pmd-kotlin-profile.xml"))
                    .restoreProfileAtStartup(ofClasspath("/com/sonar/it/java/PmdTest/pmd-kotlin-all-rules.xml"))
                    .restoreProfileAtStartup(ofClasspath("/com/sonar/it/java/PmdTest/pmd-apex-profile.xml"))
                    .build();

            return new PmdTestOrchestrator(orchestrator);
        }
        catch(Exception e) {
            System.out.println("ERROR: " + e);
            throw new RuntimeException(e);
        }
    }

    private static String deriveProjectKey(String projectName) {
        return String.format("com.sonarsource.it.projects:%s", projectName);
    }

    private static String determineJavaPluginVersion() {
        return System.getProperty(SONAR_JAVA_PLUGIN_VERSION_KEY, "LATEST_RELEASE[8.15]"); // use 8.9 to test with SQ 9.9
    }

    private static String determineKotlinPluginVersion() {
        return System.getProperty(SONAR_KOTLIN_PLUGIN_VERSION_KEY, "LATEST_RELEASE[2.15]");
    }

    private static String determineSonarqubeVersion() {
        return System.getProperty(SONAR_VERSION_KEY, "LATEST_RELEASE[25.6]"); // use SQ 9.9.4 to test with old version
    }
}

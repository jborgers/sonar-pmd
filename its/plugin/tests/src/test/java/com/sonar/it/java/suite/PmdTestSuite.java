/*
 * PMD :: IT :: Plugin :: Tests
 * Copyright (C) 2013 SonarSource
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sonar.it.java.suite;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.locator.FileLocation;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  PmdTest.class
})
public class PmdTestSuite {

  @ClassRule
  public static final Orchestrator ORCHESTRATOR = Orchestrator
    .builderEnv()
    .addPlugin("java")
    .addPlugin(FileLocation.of("../../../target/sonar-pmd-plugin.jar"))
    .addPlugin(FileLocation.of(TestUtils.pluginJar("pmd-extension-plugin")))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/java/PmdTest/pmd-junit-rules.xml"))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/java/PmdTest/pmd-extensions-profile.xml"))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/java/PmdTest/pmd-backup.xml"))
    .build();

}

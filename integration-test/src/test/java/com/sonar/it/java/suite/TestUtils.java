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

import java.io.File;

import org.apache.commons.io.FileUtils;

class TestUtils {

    private static final File HOME;

    static {

        File testResources = FileUtils.toFile(TestUtils.class.getResource("/TestUtils.txt"));
        HOME = testResources // home/tests/src/tests/resources
                .getParentFile() // home/tests/src/tests
                .getParentFile() // home/tests/src
                .getParentFile(); // home
    }

    static File projectPom(String projectName) {
        return new File(HOME, "projects/" + projectName + "/pom.xml");
    }

    static String keyFor(String projectKey, String srcDir, String pkgDir, String cls) {
        return "com.sonarsource.it.projects:" + projectKey + ":" + srcDir + pkgDir + cls;
    }

    static String keyFor(String projectKey, String pkgDir, String cls) {
        return keyFor(projectKey, "src/main/java/", pkgDir, cls + ".java");
    }

    static String keyForTest() {
        return keyFor("pmd-junit-rules", "src/test/java/", "", "ProductionCodeTest" + ".java");
    }

}

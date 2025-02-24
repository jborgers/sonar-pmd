/*
 * SonarQube PMD7 Plugin
 * Copyright (C) 2012-2021 SonarSource SA and others
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
package org.sonar.plugins.pmd;

/**
 * Common configuration parameters for the Sonar-PMD plugin.
 */
public final class PmdConstants {
    public static final String PLUGIN_NAME = "PMD";
    public static final String PLUGIN_KEY = "pmd";
    public static final String MAIN_JAVA_REPOSITORY_KEY = PLUGIN_KEY;
    public static final String MAIN_KOTLIN_REPOSITORY_KEY = "pmd-kotlin";
    public static final String REPOSITORY_NAME = "PMD";
    public static final String REPOSITORY_KOTLIN_NAME = "PMD Kotlin";
    public static final String TEST_JAVA_REPOSITORY_KEY = "pmd-unit-tests";
    public static final String TEST_REPOSITORY_NAME = "PMD Unit Tests";
    public static final String XPATH_CLASS = "net.sourceforge.pmd.lang.rule.xpath.XPathRule";
    public static final String XPATH_EXPRESSION_PARAM = "xpath";
    public static final String XPATH_MESSAGE_PARAM = "message";

    /**
     * Key of the java version used for sources
     */
    public static final String JAVA_SOURCE_VERSION = "sonar.java.source";

    /**
     * Default value for property {@link #JAVA_SOURCE_VERSION}.
     */
    public static final String JAVA_SOURCE_VERSION_DEFAULT_VALUE = "11";

    /**
     * Maximum supported value for property {@link #JAVA_SOURCE_VERSION}. For PMD 6 this is 20-preview.
     */
    public static final String JAVA_SOURCE_MAXIMUM_SUPPORTED_VALUE = "20-preview";

    /**
     * Minimum UNsupported value for property {@link #JAVA_SOURCE_VERSION}. For PMD 6 this is 21.
     */
    public static final String JAVA_SOURCE_MINIMUM_UNSUPPORTED_VALUE = "21";
    /**
     * The Java Language key.
     */
    public static final String LANGUAGE_JAVA_KEY = "java";
    public static final String LANGUAGE_KOTLIN_KEY = "kotlin";

    private PmdConstants() {
    }
}

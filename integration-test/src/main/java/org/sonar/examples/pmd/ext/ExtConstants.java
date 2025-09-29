package org.sonar.examples.pmd.ext;

public final class ExtConstants {
    private ExtConstants() {}

    public static final String REPOSITORY_KEY = "pmd-extensions";
    public static final String LANGUAGE_JAVA_KEY = "java";

    // Java source version properties (aligned with PMD 7 limits)
    public static final String JAVA_SOURCE_VERSION = "sonar.java.source";
    public static final String JAVA_SOURCE_VERSION_DEFAULT_VALUE = "24";
    public static final String JAVA_SOURCE_MAXIMUM_SUPPORTED_VALUE = "24-preview";
    public static final String JAVA_SOURCE_MINIMUM_UNSUPPORTED_VALUE = "25";
}

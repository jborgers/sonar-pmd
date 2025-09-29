package org.sonar.examples.pmd.ext;

/**
 * Minimal constants required for the PMD Extensions plugin (Java only).
 */
public final class PmdConstants {
    public static final String LANGUAGE_JAVA_KEY = "java";

    /**
     * Key of the java version used for sources
     */
    public static final String JAVA_SOURCE_VERSION = "sonar.java.source";

    /**
     * Default value for property {@link #JAVA_SOURCE_VERSION}.
     */
    public static final String JAVA_SOURCE_VERSION_DEFAULT_VALUE = "24";

    /**
     * Maximum supported value for property {@link #JAVA_SOURCE_VERSION}. For PMD 7 this is 24-preview.
     */
    public static final String JAVA_SOURCE_MAXIMUM_SUPPORTED_VALUE = "24-preview";

    /**
     * Minimum unsupported value for property {@link #JAVA_SOURCE_VERSION}. For PMD 7 this is 25.
     */
    public static final String JAVA_SOURCE_MINIMUM_UNSUPPORTED_VALUE = "25";

    private PmdConstants() {}
}

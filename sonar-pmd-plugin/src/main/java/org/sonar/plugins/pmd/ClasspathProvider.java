package org.sonar.plugins.pmd;

import java.io.File;
import java.util.Collection;

/**
 * Interface for providing classpath elements for PMD analysis.
 * This replaces the dependency on JavaResourceLocator.
 */
public interface ClasspathProvider {

    Collection<File> binaryDirs();

    Collection<File> classpath();

    Collection<File> testBinaryDirs();

    Collection<File> testClasspath();
}
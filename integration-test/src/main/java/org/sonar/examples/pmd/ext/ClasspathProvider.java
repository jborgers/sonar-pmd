package org.sonar.examples.pmd.ext;

import java.io.File;
import java.util.Collection;

/**
 * Interface for providing classpath elements for PMD analysis.
 */
public interface ClasspathProvider {

    Collection<File> binaryDirs();

    Collection<File> classpath();

    Collection<File> testBinaryDirs();

    Collection<File> testClasspath();
}

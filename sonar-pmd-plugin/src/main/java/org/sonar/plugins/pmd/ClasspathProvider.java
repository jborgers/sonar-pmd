package org.sonar.plugins.pmd;

import java.io.File;
import java.util.Collection;

/**
 * Interface for providing classpath elements for PMD analysis.
 * This replaces the dependency on JavaResourceLocator.
 */
public interface ClasspathProvider {
    
    /**
     * Returns the classpath elements for PMD analysis.
     * 
     * @return A collection of classpath elements as Files
     */
    Collection<File> classpath();
}
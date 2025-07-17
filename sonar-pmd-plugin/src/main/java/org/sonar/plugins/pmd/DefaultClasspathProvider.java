package org.sonar.plugins.pmd;

import org.sonar.api.batch.ScannerSide;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * Default implementation of ClasspathProvider that returns an empty classpath.
 * This is sufficient for most PMD analysis scenarios.
 */
@ScannerSide
public class DefaultClasspathProvider implements ClasspathProvider {

    @Override
    public Collection<File> classpath() {
        // Return an empty list as the default implementation
        // This is similar to what PmdKotlinExecutor does
        return Collections.emptyList();
    }
}
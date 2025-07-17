package org.sonar.plugins.pmd;

import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Configuration;
import org.sonar.java.classpath.ClasspathForMain;
import org.sonar.java.classpath.ClasspathForTest;

import java.io.File;
import java.util.Collection;

/**
 * Default implementation of ClasspathProvider that uses java-frontend's ClasspathForMain and ClasspathForTest
 * to provide classpath information for PMD analysis.
 *
 * Based on findbugs sonar plugin approach.
 */
@ScannerSide
public class DefaultClasspathProvider implements ClasspathProvider {

    private final ClasspathForMain classpathForMain;
    private final ClasspathForTest classpathForTest;

    public DefaultClasspathProvider(Configuration configuration, FileSystem fileSystem) {
        classpathForMain = new ClasspathForMain(configuration, fileSystem);
        classpathForTest = new ClasspathForTest(configuration, fileSystem);
    }

    @Override
    public Collection<File> binaryDirs() {
        return classpathForMain.getBinaryDirs();
    }

    @Override
    public Collection<File> classpath() {
        return classpathForMain.getElements();
    }

    @Override
    public Collection<File> testBinaryDirs() {
        return classpathForTest.getBinaryDirs();
    }

    @Override
    public Collection<File> testClasspath() {
        return classpathForTest.getElements();
    }

}

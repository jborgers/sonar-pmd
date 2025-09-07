package org.sonar.examples.pmd.ext;

import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Configuration;
import org.sonar.java.classpath.ClasspathForMain;
import org.sonar.java.classpath.ClasspathForTest;

import java.io.File;
import java.util.Collection;

@ScannerSide
public class ExtClasspathProvider {

    private final ClasspathForMain classpathForMain;
    private final ClasspathForTest classpathForTest;

    public ExtClasspathProvider(Configuration configuration, FileSystem fileSystem) {
        classpathForMain = new ClasspathForMain(configuration, fileSystem);
        classpathForTest = new ClasspathForTest(configuration, fileSystem);
    }

    public Collection<File> binaryDirs() {
        return classpathForMain.getBinaryDirs();
    }

    public Collection<File> classpath() {
        return classpathForMain.getElements();
    }

    public Collection<File> testBinaryDirs() {
        return classpathForTest.getBinaryDirs();
    }

    public Collection<File> testClasspath() {
        return classpathForTest.getElements();
    }
}

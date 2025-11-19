/*
 * SonarQube PMD7 Plugin - Apex module tests
 */
package org.sonar.plugins.pmd;

import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.reporting.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

class PmdApexExecutorTest extends AbstractPmdExecutorTest {

    private PmdApexExecutor realPmdExecutor;

    @BeforeEach
    void setUp() {
        realPmdExecutor = new PmdApexExecutor(
                fileSystem,
                activeRules,
                pmdConfiguration,
                settings.asConfig()
        );
        pmdExecutor = Mockito.spy(realPmdExecutor);
    }

    protected static DefaultInputFile fileApex(String path, Type type) {
        return TestInputFileBuilder.create("", path)
                .setType(type)
                .setLanguage(PmdConstants.LANGUAGE_APEX_KEY)
                .build();
    }

    @Override
    protected DefaultInputFile getAppropriateInputFileForTest() {
        return fileApex("src/test/apex/TestApex.cls", Type.MAIN);
    }

    @Test
    void should_execute_pmd_on_apex_source_files() {
        // Given
        DefaultInputFile srcFile = fileApex("src/test/apex/TestApex.cls", Type.MAIN);
        setupPmdRuleSet(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "simple-apex.xml");
        fileSystem.add(srcFile);

        // When
        Report report = pmdExecutor.execute();

        // Then
        assertThat(report).isNotNull();
        verify(pmdConfiguration).dumpXmlReport(report);
    }

    @Test
    void should_execute_pmd_on_apex_test_files() {
        // Given
        DefaultInputFile testFile = fileApex("src/test/apex/TestApexTest.cls", Type.TEST);
        setupPmdRuleSet(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "simple-apex.xml");
        fileSystem.add(testFile);

        // When
        Report report = pmdExecutor.execute();

        // Then
        assertThat(report).isNotNull();
        verify(pmdConfiguration).dumpXmlReport(report);
    }

    @Test
    void should_ignore_empty_apex_test_dir() {
        // Given
        DefaultInputFile srcFile = fileApex("src/test/apex/TestApex.cls", Type.MAIN);
        doReturn(pmdTemplate).when(pmdExecutor).createPmdTemplate(any(URLClassLoader.class));
        setupPmdRuleSet(PmdConstants.MAIN_APEX_REPOSITORY_KEY, "simple-apex.xml");
        fileSystem.add(srcFile);

        // When
        pmdExecutor.execute();

        // Then
        verify(pmdTemplate).process(anyIterable(), any(RuleSet.class));
        verifyNoMoreInteractions(pmdTemplate);
    }

    @Test
    void should_create_empty_classloader() throws Exception {
        // When
        pmdExecutor.execute();

        // Then
        // Verify that createPmdTemplate is called with a URLClassLoader that has no URLs
        verify(pmdExecutor).createPmdTemplate(argThat(classLoader -> 
                classLoader instanceof URLClassLoader && ((URLClassLoader) classLoader).getURLs().length == 0));
    }
}

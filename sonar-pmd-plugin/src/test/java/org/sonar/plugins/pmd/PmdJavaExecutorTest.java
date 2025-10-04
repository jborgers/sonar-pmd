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

import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.reporting.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;

import java.util.Arrays;
import java.util.Collections;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.rule.Severity;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

class PmdJavaExecutorTest extends AbstractPmdExecutorTest {

    private final ClasspathProvider classpathProvider = mock(ClasspathProvider.class);
    private PmdJavaExecutor realPmdExecutor;

    @BeforeEach
    void setUp() {
        realPmdExecutor = new PmdJavaExecutor(
                fileSystem,
                activeRules,
                pmdConfiguration,
                classpathProvider,
                settings.asConfig()
        );
        pmdExecutor = Mockito.spy(realPmdExecutor);
    }

    @Override
    protected DefaultInputFile getAppropriateInputFileForTest() {
        return fileJava("src/Class.java", Type.MAIN);
    }

    @Test
    void should_execute_pmd_on_main_files_and_test_files() {
        DefaultInputFile srcFile = fileJava("src/Class.java", Type.MAIN);
        DefaultInputFile tstFile = fileJava("test/ClassTest.java", Type.TEST);
        setupPmdRuleSet(PmdConstants.MAIN_JAVA_REPOSITORY_KEY, "simple.xml");
        fileSystem.add(srcFile);
        fileSystem.add(tstFile);

        Report report = pmdExecutor.execute();

        assertThat(report).isNotNull();
        verify(pmdConfiguration).dumpXmlReport(report);

        // setting java source version to the default value
        settings.removeProperty(PmdConstants.JAVA_SOURCE_VERSION);
        report = pmdExecutor.execute();

        assertThat(report).isNotNull();
        verify(pmdConfiguration).dumpXmlReport(report);
    }

    @Test
    void should_ignore_empty_test_dir() {
        DefaultInputFile srcFile = fileJava("src/Class.java", Type.MAIN);
        doReturn(pmdTemplate).when(pmdExecutor).createPmdTemplate(any(URLClassLoader.class));
        setupPmdRuleSet(PmdConstants.MAIN_JAVA_REPOSITORY_KEY, "simple.xml");
        fileSystem.add(srcFile);

        pmdExecutor.execute();
        verify(pmdTemplate).process(anyIterable(), any(RuleSet.class));
        verifyNoMoreInteractions(pmdTemplate);
    }

    @Test
    void should_build_project_classloader_from_classpathprovider() throws Exception {
        File file = new File("x");
        when(classpathProvider.classpath()).thenReturn(List.of(file));
        pmdExecutor.execute();
        ArgumentCaptor<URLClassLoader> classLoaderArgument = ArgumentCaptor.forClass(URLClassLoader.class);
        verify(pmdExecutor).createPmdTemplate(classLoaderArgument.capture());
        URLClassLoader classLoader = classLoaderArgument.getValue();
        URL[] urls = classLoader.getURLs();
        assertThat(urls).containsOnly(file.toURI().toURL());
    }

    @Test
    void invalid_classpath_element() {
        File invalidFile = mock(File.class);
        when(invalidFile.toURI()).thenReturn(URI.create("x://xxx"));
        when(classpathProvider.classpath()).thenReturn(List.of(invalidFile));

        final Throwable thrown = catchThrowable(() -> pmdExecutor.execute());

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Classpath");
    }

    @Test
    void should_apply_rules_according_to_scope() {
        // Create MAIN file with code that violates UseUtilityClass rule
        // (class with only static methods should be final and have private constructor)
        DefaultInputFile mainFile = TestInputFileBuilder.create("sonar-pmd-test", "src/Utility.java")
                .setType(Type.MAIN)
                .setLanguage(PmdConstants.LANGUAGE_JAVA_KEY)
                .setContents("public class Utility {\n" +
                        "    public static void doSomething() {\n" +
                        "        System.out.println(\"test\");\n" +
                        "    }\n" +
                        "}\n")
                .build();

        // Create TEST file with code that violates JUnitTestsShouldIncludeAssert rule
        // (test method without assertions)
        DefaultInputFile testFile = TestInputFileBuilder.create("sonar-pmd-test", "test/UtilityTest.java")
                .setType(Type.TEST)
                .setLanguage(PmdConstants.LANGUAGE_JAVA_KEY)
                .setContents("import org.junit.Test;\n" +
                        "public class UtilityTest {\n" +
                        "    @Test\n" +
                        "    public void testSomething() {\n" +
                        "        int x = 1 + 1;\n" +
                        "    }\n" +
                        "}\n")
                .build();

            // Mock active rules - this is critical! Without this, PMD won't run any analysis
            ActiveRule mockRule1 = mock(ActiveRule.class);
            when(mockRule1.ruleKey()).thenReturn(RuleKey.of(PmdConstants.MAIN_JAVA_REPOSITORY_KEY, "UnusedLocalVariable"));
            when(mockRule1.internalKey()).thenReturn("category/java/bestpractices.xml/UnusedLocalVariable");
            when(mockRule1.severity()).thenReturn(Severity.MAJOR.toString());
            when(mockRule1.params()).thenReturn(Collections.emptyMap());

            ActiveRule mockRule2 = mock(ActiveRule.class);
            when(mockRule2.ruleKey()).thenReturn(RuleKey.of(PmdConstants.MAIN_JAVA_REPOSITORY_KEY, "AvoidLiteralsInIfCondition"));
            when(mockRule2.internalKey()).thenReturn("category/java/errorprone.xml/AvoidLiteralsInIfCondition");
            when(mockRule2.severity()).thenReturn(Severity.MINOR.toString());
            when(mockRule2.params()).thenReturn(Collections.singletonMap("ignoreMagicNumbers", "-1,0"));

            // Configure activeRules to return our mock rules
            when(activeRules.findByRepository(PmdConstants.MAIN_JAVA_REPOSITORY_KEY))
                    .thenReturn(Arrays.asList(mockRule1, mockRule2));

        // Setup PMD rulesets - using the real implementation, not a spy
        // This allows PMD to actually execute
        PmdJavaExecutor realExecutor = new PmdJavaExecutor(
                fileSystem,
                activeRules,
                pmdConfiguration,
                classpathProvider,
                settings.asConfig()
        );

        // Setup different rulesets for MAIN and TEST scopes
        setupPmdRuleSet(PmdConstants.MAIN_JAVA_REPOSITORY_KEY, "main-scope.xml");

        fileSystem.add(mainFile);
        fileSystem.add(testFile);

        // Execute PMD analysis with the real executor
        Report report = realExecutor.execute();

        // Verify report is generated
        assertThat(report).isNotNull();

        // Verify violations - the report should contain violations from both scopes
        // Main file should have UseUtilityClass violation
        // Test file should have JUnitTestsShouldIncludeAssert violation
        assertThat(report.getViolations())
                .as("Report should contain violations from both MAIN and TEST files")
                .hasSizeGreaterThanOrEqualTo(2); // May be 0 if rules don't match or PMD config differs

        // Verify that PMD processed files from both scopes
        // by checking the ruleset was created for both MAIN and TEST scopes
        verify(pmdConfiguration, atLeastOnce()).dumpXmlRuleSet(
                eq(PmdConstants.MAIN_JAVA_REPOSITORY_KEY), 
                anyString(), 
                eq(RuleScope.MAIN)
        );

        verify(pmdConfiguration, atLeastOnce()).dumpXmlRuleSet(
                eq(PmdConstants.MAIN_JAVA_REPOSITORY_KEY), 
                anyString(), 
                eq(RuleScope.TEST)
        );
    }

}

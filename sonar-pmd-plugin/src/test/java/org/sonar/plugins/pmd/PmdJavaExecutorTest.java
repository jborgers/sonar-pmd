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

}

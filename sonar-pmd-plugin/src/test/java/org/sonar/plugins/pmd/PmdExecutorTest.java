/*
 * SonarQube PMD Plugin
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

import com.google.common.collect.ImmutableList;
import net.sourceforge.pmd.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.java.api.JavaResourceLocator;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PmdExecutorTest {

    private final DefaultFileSystem fileSystem = new DefaultFileSystem(new File("."));
    private final ActiveRules activeRules = mock(ActiveRules.class);
    private final PmdConfiguration pmdConfiguration = mock(PmdConfiguration.class);
    private final PmdTemplate pmdTemplate = mock(PmdTemplate.class);
    private final JavaResourceLocator javaResourceLocator = mock(JavaResourceLocator.class);
    private final MapSettings settings = new MapSettings();
    private final PmdExecutor realPmdExecutor = new PmdExecutor(
            fileSystem,
            activeRules,
            pmdConfiguration,
            javaResourceLocator,
            settings.asConfig()
    );

    private PmdExecutor pmdExecutor;

    private static DefaultInputFile file(String path, Type type) {
        return TestInputFileBuilder.create("sonar-pmd-test", path)
                .setType(type)
                .setLanguage(PmdConstants.LANGUAGE_KEY)
                .build();
    }

    @BeforeEach
    void setUp() {
        pmdExecutor = Mockito.spy(realPmdExecutor);
        fileSystem.setEncoding(StandardCharsets.UTF_8);
        settings.setProperty(PmdConstants.JAVA_SOURCE_VERSION, "1.8");
    }

    @Test
    void whenNoFilesToAnalyzeThenExecutionSucceedsWithBlankReport() {

        // when
        final Report result = pmdExecutor.execute();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getViolations())
                .isEmpty();
        assertThat(result.getProcessingErrors())
                .isEmpty();
    }

    @Test
    void should_execute_pmd_on_source_files_and_test_files() {
        DefaultInputFile srcFile = file("src/Class.java", Type.MAIN);
        DefaultInputFile tstFile = file("test/ClassTest.java", Type.TEST);
        setupPmdRuleSet(PmdConstants.REPOSITORY_KEY, "simple.xml");
        setupPmdRuleSet(PmdConstants.TEST_REPOSITORY_KEY, "junit.xml");
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
        DefaultInputFile srcFile = file("src/Class.java", Type.MAIN);
        doReturn(pmdTemplate).when(pmdExecutor).createPmdTemplate(any(URLClassLoader.class));
        setupPmdRuleSet(PmdConstants.REPOSITORY_KEY, "simple.xml");
        fileSystem.add(srcFile);

        pmdExecutor.execute();
        verify(pmdTemplate).process(anyIterable(), any(RuleSet.class));
        verifyNoMoreInteractions(pmdTemplate);
    }

    @Test
    void should_build_project_classloader_from_javaresourcelocator() throws Exception {
        File file = new File("x");
        when(javaResourceLocator.classpath()).thenReturn(ImmutableList.of(file));
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
        when(javaResourceLocator.classpath()).thenReturn(ImmutableList.of(invalidFile));

        final Throwable thrown = catchThrowable(() -> pmdExecutor.execute());

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Classpath");
    }

    @Test
    void unknown_pmd_ruleset() {
        when(pmdConfiguration.dumpXmlRuleSet(eq(PmdConstants.REPOSITORY_KEY), anyString())).thenReturn(new File("unknown"));

        DefaultInputFile srcFile = file("src/Class.java", Type.MAIN);
        fileSystem.add(srcFile);

        final Throwable thrown = catchThrowable(() -> pmdExecutor.execute());

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(RuleSetLoadException.class);
    }

    private void setupPmdRuleSet(String repositoryKey, String profileFileName) {
        final Path sourcePath = Paths.get("src/test/resources/org/sonar/plugins/pmd/").resolve(profileFileName);
        when(pmdConfiguration.dumpXmlRuleSet(eq(repositoryKey), anyString())).thenReturn(sourcePath.toFile());
    }
}

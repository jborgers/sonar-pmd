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

import net.sourceforge.pmd.lang.rule.RuleSetLoadException;
import net.sourceforge.pmd.reporting.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.rule.RuleScope;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Abstract base class for PMD executor tests with common test functionality.
 */
public abstract class AbstractPmdExecutorTest {

    protected final DefaultFileSystem fileSystem = new DefaultFileSystem(new File("."));
    protected final ActiveRules activeRules = mock(ActiveRules.class);
    protected final PmdConfiguration pmdConfiguration = mock(PmdConfiguration.class);
    protected final PmdTemplate pmdTemplate = mock(PmdTemplate.class);
    protected final MapSettings settings = new MapSettings();
    
    protected AbstractPmdExecutor pmdExecutor;

    protected static DefaultInputFile fileJava(String path, Type type) {
        return TestInputFileBuilder.create("sonar-pmd-test", path)
                .setType(type)
                .setLanguage(PmdConstants.LANGUAGE_JAVA_KEY)
                .build();
    }

    protected static DefaultInputFile fileKotlin(String path, Type type) {
        return TestInputFileBuilder.create("", path)
                .setType(type)
                .setLanguage(PmdConstants.LANGUAGE_KOTLIN_KEY)
                .build();
    }

    @BeforeEach
    void setUpAbstractTest() {
        fileSystem.setEncoding(StandardCharsets.UTF_8);
        settings.setProperty(PmdConstants.JAVA_SOURCE_VERSION, "1.8");
        
        // The concrete test class must initialize pmdExecutor in its own @BeforeEach method
    }

    @Test
    void whenNoFilesToAnalyzeThenExecutionSucceedsWithBlankReport() {
        // when
        final Report result = pmdExecutor.execute();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getViolations()).isEmpty();
        assertThat(result.getProcessingErrors()).isEmpty();
    }

    @Test
    void unknown_pmd_ruleset() {
        when(pmdConfiguration.dumpXmlRuleSet(anyString(), anyString(), ArgumentMatchers.any(RuleScope.class))).thenReturn(new File("unknown"));

        DefaultInputFile srcFile = getAppropriateInputFileForTest();
        fileSystem.add(srcFile);

        final Throwable thrown = catchThrowable(() -> pmdExecutor.execute());

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(RuleSetLoadException.class);
    }

    /**
     * Get an appropriate input file for the specific executor being tested
     */
    protected abstract DefaultInputFile getAppropriateInputFileForTest();

    protected void setupPmdRuleSet(String repositoryKey, String profileFileName) {
        final Path sourcePath = Paths.get("src/test/resources/org/sonar/plugins/pmd/").resolve(profileFileName);
        when(pmdConfiguration.dumpXmlRuleSet(eq(repositoryKey), anyString(), ArgumentMatchers.any(RuleScope.class))).thenReturn(sourcePath.toFile());
    }
}
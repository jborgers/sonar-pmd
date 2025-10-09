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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.rule.RuleScope;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifies that the extra information provided by PMD's RuleSetLoader (via PmdReporter)
 * is visible in logging (stderr) when ruleset loading fails.
 */
class RuleSetLoaderLoggingTest {

    private final MapSettings settings = new MapSettings();
    private PmdJavaExecutor executor;

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        // Minimal file system with one Java file to trigger execution
        var fs = new org.sonar.api.batch.fs.internal.DefaultFileSystem(new File("."));
        fs.setEncoding(StandardCharsets.UTF_8);
        DefaultInputFile src = TestInputFileBuilder.create("sonar-pmd-test", "src/Class.java")
            .setLanguage(PmdConstants.LANGUAGE_JAVA_KEY)
            .setType(InputFile.Type.MAIN)
            .build();
        fs.add(src);

        var activeRules = org.mockito.Mockito.mock(org.sonar.api.batch.rule.ActiveRules.class);
        var pmdConfig = org.mockito.Mockito.mock(PmdConfiguration.class);
        var classpathProvider = org.mockito.Mockito.mock(ClasspathProvider.class);

        // Point the dumped ruleset to our intentionally invalid ruleset file
        Path invalid = Paths.get("src/test/resources/org/sonar/plugins/pmd/invalid-ref.xml");
        when(pmdConfig.dumpXmlRuleSet(eq(PmdConstants.MAIN_JAVA_REPOSITORY_KEY), anyString(), eq(RuleScope.MAIN)))
            .thenReturn(invalid.toFile());
        when(pmdConfig.dumpXmlRuleSet(eq(PmdConstants.MAIN_JAVA_REPOSITORY_KEY), anyString(), eq(RuleScope.TEST)))
            .thenReturn(invalid.toFile());

        executor = Mockito.spy(new PmdJavaExecutor(fs, activeRules, pmdConfig, classpathProvider, settings.asConfig()));

        // Capture stderr where AbstractPmdExecutor writes reporter output
        originalErr = System.err;
        System.setErr(new PrintStream(errContent, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setErr(originalErr);
    }

    @Test
    void should_log_rule_set_loader_details_to_stderr() {
        Throwable thrown = catchThrowable(() -> executor.execute());

        // We still expect the wrapped RuleSetLoadException as before
        assertThat(thrown)
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(RuleSetLoadException.class);

        // And now verify that additional details were printed to stderr by the reporter
        String stderr = errContent.toString(StandardCharsets.UTF_8);
        // Robust check: must contain our bogus rule id, which should be echoed by PMD's diagnostics
        assertThat(stderr)
            .isNotBlank()
            .contains("ThisRuleDoesNotExist");
    }
}

/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PMDException;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.SourceCodeProcessor;
import net.sourceforge.pmd.lang.java.Java13Handler;
import net.sourceforge.pmd.lang.java.Java15Handler;
import net.sourceforge.pmd.lang.java.Java16Handler;
import net.sourceforge.pmd.lang.java.Java17Handler;
import net.sourceforge.pmd.lang.java.Java18Handler;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PmdTemplateTest {

    private final RuleSets rulesets = mock(RuleSets.class);
    private final RuleContext ruleContext = mock(RuleContext.class);
    private final PMDConfiguration configuration = mock(PMDConfiguration.class);
    private final SourceCodeProcessor processor = mock(SourceCodeProcessor.class);
    private final InputFile inputFile = TestInputFileBuilder.create(
            "src",
            "test/resources/org/sonar/plugins/pmd/source.txt"
    ).build();

    @Test
    void should_process_input_file() throws Exception {
        doAnswer((Answer<Void>) invocation -> {
            final InputStream inputStreamArg = (InputStream) invocation.getArguments()[0];
            final List<String> inputStreamLines =
                    new BufferedReader(new InputStreamReader(inputStreamArg))
                            .lines()
                            .collect(Collectors.toList());
            assertThat(inputStreamLines).containsExactly("Example source");
            return null;
        }).when(processor).processSourceCode(any(InputStream.class), eq(rulesets), eq(ruleContext));

        new PmdTemplate(configuration, processor).process(inputFile, rulesets, ruleContext);

        verify(ruleContext).setSourceCodeFilename(inputFile.uri().toString());
        verify(processor).processSourceCode(any(InputStream.class), eq(rulesets), eq(ruleContext));
    }

    @Test
    void should_ignore_PMD_error() throws PMDException {
        doThrow(new PMDException("BUG"))
                .when(processor).processSourceCode(any(InputStream.class), any(RuleSets.class), any(RuleContext.class));

        new PmdTemplate(configuration, processor).process(inputFile, rulesets, ruleContext);
    }

    @Test
    void java12_version() {
        assertThat(PmdTemplate.languageVersion("1.2").getLanguageVersionHandler()).isInstanceOf(Java13Handler.class);
    }

    @Test
    void java5_version() {
        assertThat(PmdTemplate.languageVersion("5").getLanguageVersionHandler()).isInstanceOf(Java15Handler.class);
    }

    @Test
    void java6_version() {
        assertThat(PmdTemplate.languageVersion("6").getLanguageVersionHandler()).isInstanceOf(Java16Handler.class);
    }

    @Test
    void java7_version() {
        assertThat(PmdTemplate.languageVersion("7").getLanguageVersionHandler()).isInstanceOf(Java17Handler.class);
    }

    @Test
    void java8_version() {
        assertThat(PmdTemplate.languageVersion("8").getLanguageVersionHandler()).isInstanceOf(Java18Handler.class);
    }

    @Test
    void should_fail_on_invalid_java_version() {
        final Throwable thrown = catchThrowable(() -> PmdTemplate.create("12.2", mock(ClassLoader.class), StandardCharsets.UTF_8));
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldnt_fail_on_valid_java_version() {
        PmdTemplate.create("6", mock(ClassLoader.class), StandardCharsets.UTF_8);
    }

    /**
     * SONARPLUGINS-3318
     */
    @Test
    void should_set_classloader() {
        ClassLoader classloader = mock(ClassLoader.class);
        PmdTemplate pmdTemplate = PmdTemplate.create("6", classloader, StandardCharsets.UTF_8);
        assertThat(pmdTemplate.configuration().getClassLoader()).isEqualTo(classloader);
    }

    @Test
    void should_set_encoding() {
        PmdTemplate pmdTemplate = PmdTemplate.create("6", mock(ClassLoader.class), StandardCharsets.UTF_16BE);
        assertThat(pmdTemplate.configuration().getSourceEncoding()).isEqualTo("UTF-16BE");
    }

}

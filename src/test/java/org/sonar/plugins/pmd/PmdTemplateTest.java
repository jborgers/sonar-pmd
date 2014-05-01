/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.pmd;

import net.sourceforge.pmd.lang.LanguageVersion;

import net.sourceforge.pmd.SourceCodeProcessor;
import net.sourceforge.pmd.PMDConfiguration;
import com.google.common.base.Charsets;
import net.sourceforge.pmd.PMDException;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSets;
import org.junit.Test;
import org.sonar.api.resources.InputFile;
import org.sonar.api.utils.SonarException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PmdTemplateTest {
  InputFile inputFile = mock(InputFile.class);
  RuleSets rulesets = mock(RuleSets.class);
  RuleContext ruleContext = mock(RuleContext.class);
  InputStream inputStream = mock(InputStream.class);
  PMDConfiguration configuration = mock(PMDConfiguration.class);
  SourceCodeProcessor processor = mock(SourceCodeProcessor.class);

  @Test
  public void should_process_input_file() throws PMDException, FileNotFoundException {
    when(inputFile.getFile()).thenReturn(new File("source.java"));
    when(inputFile.getInputStream()).thenReturn(inputStream);

    new PmdTemplate(configuration, processor).process(inputFile, rulesets, ruleContext);

    verify(ruleContext).setSourceCodeFilename(new File("source.java").getAbsolutePath());
    verify(processor).processSourceCode(inputStream, rulesets, ruleContext);
  }

  @Test
  public void should_ignore_PMD_error() throws PMDException, FileNotFoundException {
    when(inputFile.getFile()).thenReturn(new File("source.java"));
    when(inputFile.getInputStream()).thenReturn(inputStream);
    doThrow(new PMDException("BUG")).when(processor).processSourceCode(inputStream, rulesets, ruleContext);

    new PmdTemplate(configuration, processor).process(inputFile, rulesets, ruleContext);
  }

  @Test
  public void java11_version() {
    assertThat(PmdTemplate.languageVersion("1.1")).isEqualTo(LanguageVersion.JAVA_13);
  }

  @Test
  public void java12_version() {
    assertThat(PmdTemplate.languageVersion("1.2")).isEqualTo(LanguageVersion.JAVA_13);
  }

  @Test
  public void java5_version() {
    assertThat(PmdTemplate.languageVersion("5")).isEqualTo(LanguageVersion.JAVA_15);
  }

  @Test
  public void java6_version() {
    assertThat(PmdTemplate.languageVersion("6")).isEqualTo(LanguageVersion.JAVA_16);
  }

  @Test
  public void java7_version() {
    assertThat(PmdTemplate.languageVersion("7")).isEqualTo(LanguageVersion.JAVA_17);
  }
  
  @Test
  public void java8_version() {
    assertThat(PmdTemplate.languageVersion("8")).isEqualTo(LanguageVersion.JAVA_18);
  }

  @Test(expected = SonarException.class)
  public void should_fail_on_invalid_java_version() {
    PmdTemplate.create("12.2", mock(ClassLoader.class), Charsets.UTF_8);
  }

  @Test
  public void shouldnt_fail_on_valid_java_version() {
    PmdTemplate.create("6", mock(ClassLoader.class), Charsets.UTF_8);
  }

  /**
   * SONARPLUGINS-3318
   */
  @Test
  public void should_set_classloader() {
    ClassLoader classloader = mock(ClassLoader.class);
    PmdTemplate pmdTemplate = PmdTemplate.create("6", classloader, Charsets.UTF_8);
    assertThat(pmdTemplate.configuration().getClassLoader()).isEqualTo(classloader);
  }

  @Test
  public void should_set_encoding() {
    PmdTemplate pmdTemplate = PmdTemplate.create("6", mock(ClassLoader.class), Charsets.UTF_16BE);
    assertThat(pmdTemplate.configuration().getSourceEncoding()).isEqualTo("UTF-16BE");
  }
  
}

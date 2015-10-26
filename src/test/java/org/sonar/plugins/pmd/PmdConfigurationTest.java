/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.sourceforge.pmd.Report;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class PmdConfigurationTest {
  PmdConfiguration configuration;

  Settings settings = new Settings();
  FileSystem fs = mock(FileSystem.class);

  private static final File WORK_DIR = new File("test-work-dir");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @BeforeClass
  public static void createTempDir() {
    deleteTempDir();
    WORK_DIR.mkdir();
  }

  @AfterClass
  public static void deleteTempDir() {
    if (WORK_DIR.exists()) {
      for (File file : WORK_DIR.listFiles()) {
        file.delete();
      }
      WORK_DIR.delete();
    }
  }

  @Before
  public void setUpPmdConfiguration() {
    configuration = new PmdConfiguration(fs, settings);
  }

  @Test
  public void should_dump_xml_rule_set() throws IOException {
    when(fs.workDir()).thenReturn(WORK_DIR);

    File rulesFile = configuration.dumpXmlRuleSet("pmd", "<rules>");

    assertThat(rulesFile).isEqualTo(new File(WORK_DIR, "pmd.xml"));
    assertThat(Files.readLines(rulesFile, Charsets.UTF_8)).containsExactly("<rules>");
  }

  @Test
  public void should_fail_to_dump_xml_rule_set() throws IOException {
    when(fs.workDir()).thenReturn(new File("xxx"));

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("Fail to save the PMD configuration");

    configuration.dumpXmlRuleSet("pmd", "<xml>");
  }

  @Test
  public void should_dump_xml_report() throws IOException {
    when(fs.workDir()).thenReturn(WORK_DIR);

    settings.setProperty(PmdConfiguration.PROPERTY_GENERATE_XML, true);
    File reportFile = configuration.dumpXmlReport(new Report());

    assertThat(reportFile).isEqualTo(new File(WORK_DIR, "pmd-result.xml"));
    List<String> writtenLines = Files.readLines(reportFile, Charsets.UTF_8);
    assertThat(writtenLines).hasSize(3);
    assertThat(writtenLines.get(1)).contains("<pmd");
  }

  @Test
  public void should_fail_to_dump_xml_report() throws Exception {
    when(fs.workDir()).thenReturn(new File("xxx"));

    settings.setProperty(PmdConfiguration.PROPERTY_GENERATE_XML, true);

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("Fail to save the PMD report");

    configuration.dumpXmlReport(new Report());
  }

  @Test
  public void should_ignore_xml_report_when_property_is_not_set() {
    File reportFile = configuration.dumpXmlReport(new Report());

    assertThat(reportFile).isNull();
    verifyZeroInteractions(fs);
  }

}

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
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.XMLRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

public class PmdConfiguration implements BatchExtension {
  private static final Logger LOG = LoggerFactory.getLogger(PmdConfiguration.class);

  public static final String PROPERTY_GENERATE_XML = "sonar.pmd.generateXml";
  public static final String PMD_RESULT_XML = "pmd-result.xml";

  private final FileSystem fileSystem;
  private final Settings settings;

  public PmdConfiguration(FileSystem fileSystem, Settings settings) {
    this.fileSystem = fileSystem;
    this.settings = settings;
  }

  public File dumpXmlRuleSet(String repositoryKey, String rulesXml) {
    try {
      File configurationFile = writeToWorkingDirectory(rulesXml, repositoryKey + ".xml");

      LOG.info("PMD configuration: " + configurationFile.getAbsolutePath());

      return configurationFile;
    } catch (IOException e) {
      throw new IllegalStateException("Fail to save the PMD configuration", e);
    }
  }

  public File dumpXmlReport(Report report) {
    if (!settings.getBoolean(PROPERTY_GENERATE_XML)) {
      return null;
    }

    try {
      String reportAsString = reportToString(report);

      File reportFile = writeToWorkingDirectory(reportAsString, PMD_RESULT_XML);

      LOG.info("PMD output report: " + reportFile.getAbsolutePath());

      return reportFile;
    } catch (IOException e) {
      throw new IllegalStateException("Fail to save the PMD report", e);
    }
  }

  private static String reportToString(Report report) throws IOException {
    StringWriter output = new StringWriter();

    Renderer xmlRenderer = new XMLRenderer();
    xmlRenderer.setWriter(output);
    xmlRenderer.start();
    xmlRenderer.renderFileReport(report);
    xmlRenderer.end();
  
    return output.toString();
  }

  private File writeToWorkingDirectory(String content, String fileName) throws IOException {
    File file = new File(fileSystem.workDir(), fileName);
    Files.write(content, file, Charsets.UTF_8);
    return file;
  }

}

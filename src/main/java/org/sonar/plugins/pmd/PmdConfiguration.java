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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.XMLRenderer;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

@ScannerSide
public class PmdConfiguration {
    static final String PROPERTY_GENERATE_XML = "sonar.pmd.generateXml";
    private static final String PMD_RESULT_XML = "pmd-result.xml";
    private static final Logger LOG = Loggers.get(PmdConfiguration.class);
    private final FileSystem fileSystem;
    private final Configuration settings;

    public PmdConfiguration(FileSystem fileSystem, Configuration settings) {
        this.fileSystem = fileSystem;
        this.settings = settings;
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

    File dumpXmlRuleSet(String repositoryKey, String rulesXml) {
        try {
            File configurationFile = writeToWorkingDirectory(rulesXml, repositoryKey + ".xml").toFile();

            LOG.info("PMD configuration: " + configurationFile.getAbsolutePath());

            return configurationFile;
        } catch (IOException e) {
            throw new IllegalStateException("Fail to save the PMD configuration", e);
        }
    }

    /**
     * Writes an XML Report about the analyzed project into the current working directory
     * unless <code>sonar.pmd.generateXml</code> is set to false.
     *
     * @param report The report which shall be written into an XML file.
     * @return The file reference to the XML document.
     */
    Path dumpXmlReport(Report report) {
        if (!settings.getBoolean(PROPERTY_GENERATE_XML).orElse(false)) {
            return null;
        }

        try {
            final String reportAsString = reportToString(report);
            final Path reportFile = writeToWorkingDirectory(reportAsString, PMD_RESULT_XML);

            LOG.info("PMD output report: " + reportFile.toString());

            return reportFile;
        } catch (IOException e) {
            throw new IllegalStateException("Fail to save the PMD report", e);
        }
    }

    private Path writeToWorkingDirectory(String content, String fileName) throws IOException {
        final Path targetPath = fileSystem.workDir().toPath().resolve(fileName);
        Files.write(targetPath, content.getBytes());

        return targetPath;
    }
}

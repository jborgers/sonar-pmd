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

import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.XMLRenderer;
import net.sourceforge.pmd.reporting.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleScope;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

@ScannerSide
public class PmdConfiguration {
    static final String PROPERTY_GENERATE_XML = "sonar.pmd.generateXml";
    private static final String PMD_RESULT_XML = "pmd-result.xml";
    private static final Logger LOG = LoggerFactory.getLogger(PmdConfiguration.class);
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

    File dumpXmlRuleSet(String repositoryKey, String rulesXml, RuleScope scope) {
        try {
            String suffix;
            switch (scope) {
                case MAIN:
                    suffix = "-main";
                    break;
                case TEST:
                    suffix = "-test";
                    break;
                case ALL:
                default:
                    suffix = "";
                    break;
            }
            String fileName = repositoryKey + suffix + ".xml";
            File configurationFile = writeToWorkingDirectory(rulesXml, fileName).toFile();

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

            LOG.info("PMD output report: {}", reportFile);

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

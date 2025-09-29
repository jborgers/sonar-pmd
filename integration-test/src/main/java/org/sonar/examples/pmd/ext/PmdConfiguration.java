package org.sonar.examples.pmd.ext;

import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.XMLRenderer;
import net.sourceforge.pmd.reporting.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Configuration;

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

    File dumpXmlRuleSet(String repositoryKey, String rulesXml) {
        try {
            File configurationFile = writeToWorkingDirectory(rulesXml, repositoryKey + ".xml").toFile();

            LOG.info("PMD configuration: " + configurationFile.getAbsolutePath());

            return configurationFile;
        } catch (IOException e) {
            throw new IllegalStateException("Fail to save the PMD configuration", e);
        }
    }

    Path dumpXmlReport(Report report) {
        if (!settings.getBoolean(PROPERTY_GENERATE_XML).orElse(false)) {
            return null;
        }

        try {
            final String reportAsString = reportToString(report);
            final Path reportFile = writeToWorkingDirectory(reportAsString, PMD_RESULT_XML);

            LOG.info("PMD output report: {}" + reportFile);

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

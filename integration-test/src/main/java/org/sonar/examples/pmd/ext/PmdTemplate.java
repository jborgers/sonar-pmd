package org.sonar.examples.pmd.ext;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.renderers.EmptyRenderer;
import net.sourceforge.pmd.reporting.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;

import java.nio.charset.Charset;
import java.nio.file.Paths;

public class PmdTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(PmdTemplate.class);

    private final PMDConfiguration configuration;

    PmdTemplate(PMDConfiguration configuration) {
        this.configuration = configuration;
    }

    public static PmdTemplate create(String javaVersion, ClassLoader classloader, Charset charset) {
        PMDConfiguration configuration = new PMDConfiguration();

        LanguageVersion javaLanguageVersion = new JavaLanguageModule().getVersion(javaVersion);
        if (javaLanguageVersion == null) {
            throw new IllegalArgumentException("Unsupported Java version for PMD: " + javaVersion);
        }
        configuration.setDefaultLanguageVersion(javaLanguageVersion);
        LOG.info("Set default language version to Java: " + javaLanguageVersion.getName());

        configuration.setClassLoader(classloader);
        configuration.setSourceEncoding(charset);
        configuration.setFailOnViolation(false);
        configuration.setIgnoreIncrementalAnalysis(true);
        configuration.setReportFormat(EmptyRenderer.NAME);

        return new PmdTemplate(configuration);
    }

    PMDConfiguration configuration() {
        return configuration;
    }

    public Report process(Iterable<InputFile> files, RuleSet ruleset) {
        try (PmdAnalysis pmd = PmdAnalysis.create(configuration)) {
            pmd.addRuleSet(ruleset);
            for (InputFile file : files) {
                pmd.files().addFile(Paths.get(file.uri()));
            }
            return pmd.performAnalysisAndCollectReport();
        }
    }
}

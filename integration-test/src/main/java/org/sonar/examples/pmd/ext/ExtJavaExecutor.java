package org.sonar.examples.pmd.ext;

import net.sourceforge.pmd.PMDVersion;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.lang.rule.RuleSetLoadException;
import net.sourceforge.pmd.lang.rule.RuleSetLoader;
import net.sourceforge.pmd.reporting.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

@ScannerSide
public class ExtJavaExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(ExtJavaExecutor.class);

    private final FileSystem fs;
    private final ActiveRules rulesProfile;
    private final ExtPmdConfiguration pmdConfiguration;
    private final Configuration settings;
    private final ExtClasspathProvider classpathProvider;

    public ExtJavaExecutor(FileSystem fileSystem,
                           ActiveRules rulesProfile,
                           ExtPmdConfiguration pmdConfiguration,
                           ExtClasspathProvider classpathProvider,
                           Configuration settings) {
        this.fs = fileSystem;
        this.rulesProfile = rulesProfile;
        this.pmdConfiguration = pmdConfiguration;
        this.classpathProvider = classpathProvider;
        this.settings = settings;
    }

    public Report execute() {
        final long startTimeMs = System.currentTimeMillis();
        LOG.info("Execute PMD {}", PMDVersion.VERSION);
        final ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();

        try (URLClassLoader classLoader = createClassloader()) {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            Report mainReport = executePmd(classLoader, Type.MAIN);
            Report testReport = executePmd(classLoader, Type.TEST);

            if (mainReport == null) return testReport;
            if (testReport == null) return mainReport;

            // Union reports
            // PMD 7 Report has union operation by providing listener; easiest is to return main if test is empty
            // For simplicity, we ignore union of errors; violations will be saved downstream from both reports when called separately
            return mainReport.union(testReport);
        } catch (IOException e) {
            LOG.error("Failed to close URLClassLoader.", e);
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(initialClassLoader);
            LOG.info("Execute PMD {} (done) | time={}ms", PMDVersion.VERSION, System.currentTimeMillis() - startTimeMs);
        }
    }

    private Report executePmd(URLClassLoader classLoader, Type type) {
        Iterable<InputFile> files = filesOfType(type);
        if (!files.iterator().hasNext()) {
            return null;
        }

        RuleSet ruleSet = createRuleSet();
        if (ruleSet == null || ruleSet.size() < 1) {
            return null;
        }

        PmdTemplate pmdFactory = PmdTemplate.create(getSourceVersion(), classLoader, fs.encoding());
        Report report = pmdFactory.process(files, ruleSet);
        pmdConfiguration.dumpXmlReport(report);
        return report;
    }

    private Iterable<InputFile> filesOfType(Type fileType) {
        final FilePredicates predicates = fs.predicates();
        return fs.inputFiles(
                predicates.and(
                        predicates.hasLanguage(ExtConstants.LANGUAGE_JAVA_KEY),
                        predicates.hasType(fileType)
                )
        );
    }

    private URLClassLoader createClassloader() {
        Collection<File> classpathElements = classpathProvider.classpath();
        List<URL> urls = new ArrayList<>();
        for (File file : classpathElements) {
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Failed to create the project classloader. Classpath element is invalid: " + file, e);
            }
        }
        return new URLClassLoader(urls.toArray(new URL[0]));
    }

    private RuleSet createRuleSet() {
        // Build a minimal ruleset XML from ActiveRules (matching repository pmd-extensions)
        Collection<ActiveRule> active = rulesProfile.findByRepository(ExtConstants.REPOSITORY_KEY);
        if (active == null || active.isEmpty()) {
            return null;
        }
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\"?>\n");
        xml.append("<ruleset name=\"pmd-extensions-active\" xmlns=\"http://pmd.sourceforge.net/ruleset/2.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://pmd.sourceforge.net/ruleset/2.0.0 http://pmd.sourceforge.net/ruleset_2_0_0.xsd\">\n");
        for (ActiveRule ar : active) {
            String key = ar.ruleKey().rule();
            xml.append("  <rule ref=\"org/sonar/examples/pmd/rulesets.xml/").append(key).append("\">");
            if (!ar.params().isEmpty()) {
                xml.append("<properties>");
                ar.params().forEach((k, v) -> {
                    if (v != null) {
                        xml.append("<property name=\"").append(k).append("\" value=\"").append(escapeXml(v)).append("\"/>");
                    }
                });
                xml.append("</properties>");
            }
            xml.append("</rule>\n");
        }
        xml.append("</ruleset>\n");
        File ruleSetFile = pmdConfiguration.dumpXmlRuleSet(ExtConstants.REPOSITORY_KEY, xml.toString());
        try {
            return new RuleSetLoader().loadFromResource(ruleSetFile.getAbsolutePath());
        } catch (RuleSetLoadException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String getSourceVersion() {
        String reqJavaVersion = settings.get(ExtConstants.JAVA_SOURCE_VERSION).orElse(ExtConstants.JAVA_SOURCE_VERSION_DEFAULT_VALUE);
        String bareReqJavaVersion = reqJavaVersion;
        if (reqJavaVersion.endsWith("-preview")) {
            bareReqJavaVersion = reqJavaVersion.substring(0, reqJavaVersion.indexOf("-preview"));
        }
        String effectiveJavaVersion = bareReqJavaVersion;
        if (Float.parseFloat(bareReqJavaVersion) >= Float.parseFloat(ExtConstants.JAVA_SOURCE_MINIMUM_UNSUPPORTED_VALUE)) {
            effectiveJavaVersion = ExtConstants.JAVA_SOURCE_MAXIMUM_SUPPORTED_VALUE;
            LOG.warn("Requested Java version {} ('{}') is not supported by PMD. Using maximum supported version: {}.",
                    reqJavaVersion, ExtConstants.JAVA_SOURCE_VERSION, ExtConstants.JAVA_SOURCE_MAXIMUM_SUPPORTED_VALUE);
        }
        return effectiveJavaVersion;
    }
}

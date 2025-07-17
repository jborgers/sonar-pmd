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

import net.sourceforge.pmd.PMDVersion;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.lang.rule.RuleSetLoadException;
import net.sourceforge.pmd.lang.rule.RuleSetLoader;
import net.sourceforge.pmd.reporting.FileAnalysisListener;
import net.sourceforge.pmd.reporting.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.pmd.xml.PmdRuleSet;
import org.sonar.plugins.pmd.xml.PmdRuleSets;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@ScannerSide
public class PmdExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PmdExecutor.class);

    private final FileSystem fs;
    private final ActiveRules rulesProfile;
    private final PmdConfiguration pmdConfiguration;
    private final ClasspathProvider classpathProvider;
    private final Configuration settings;

    public PmdExecutor(FileSystem fileSystem, ActiveRules rulesProfile,
                       PmdConfiguration pmdConfiguration, ClasspathProvider classpathProvider, Configuration settings) {
        this.fs = fileSystem;
        this.rulesProfile = rulesProfile;
        this.pmdConfiguration = pmdConfiguration;
        this.classpathProvider = classpathProvider;
        this.settings = settings;
    }

    private static void accept(FileAnalysisListener fal) {
        LOGGER.debug("Got FileAnalysisListener: {}", fal);
    }

    public Report execute() {
        final long startTimeMs = System.currentTimeMillis();
        LOGGER.info("Execute PMD {}", PMDVersion.VERSION);
        final ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();

        try (URLClassLoader classLoader = createClassloader()) {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return executePmd(classLoader);
        } catch (IOException e) {
            LOGGER.error("Failed to close URLClassLoader.", e);
        } finally {
            Thread.currentThread().setContextClassLoader(initialClassLoader);
            LOGGER.info("Execute PMD {} (done) | time={}ms", PMDVersion.VERSION, System.currentTimeMillis() - startTimeMs);
        }

        return null;
    }

    private Report executePmd(URLClassLoader classLoader) {

        final PmdTemplate pmdFactory = createPmdTemplate(classLoader);
        final Optional<Report> javaMainReport = executeRules(pmdFactory, hasFiles(Type.MAIN, PmdConstants.LANGUAGE_JAVA_KEY), PmdConstants.MAIN_JAVA_REPOSITORY_KEY);
        final Optional<Report> javaTestReport = executeRules(pmdFactory, hasFiles(Type.TEST, PmdConstants.LANGUAGE_JAVA_KEY), PmdConstants.MAIN_JAVA_REPOSITORY_KEY);
        final Optional<Report> kotlinMainReport = executeRules(pmdFactory, hasFiles(Type.MAIN, PmdConstants.LANGUAGE_KOTLIN_KEY), PmdConstants.MAIN_KOTLIN_REPOSITORY_KEY);
        final Optional<Report> kotlinTestReport = executeRules(pmdFactory, hasFiles(Type.TEST, PmdConstants.LANGUAGE_KOTLIN_KEY), PmdConstants.MAIN_KOTLIN_REPOSITORY_KEY);

        if (LOGGER.isDebugEnabled()) {
            javaMainReport.ifPresent(this::writeDebugLine);
            javaTestReport.ifPresent(this::writeDebugLine);
            kotlinMainReport.ifPresent(this::writeDebugLine);
            kotlinTestReport.ifPresent(this::writeDebugLine);
        }

        Consumer<FileAnalysisListener> fileAnalysisListenerConsumer = PmdExecutor::accept;

        Report unionReport = Report.buildReport(fileAnalysisListenerConsumer);
        unionReport = javaMainReport.map(unionReport::union).orElse(unionReport);
        unionReport = javaTestReport.map(unionReport::union).orElse(unionReport);
        unionReport = kotlinMainReport.map(unionReport::union).orElse(unionReport);
        unionReport = kotlinTestReport.map(unionReport::union).orElse(unionReport);

        pmdConfiguration.dumpXmlReport(unionReport);

        return unionReport;
    }

    private void writeDebugLine(Report r) {
        LOGGER.debug("Report (violations, suppressedViolations, processingErrors, configurationErrors): {}, {}, {}, {}", r.getViolations().size(), r.getSuppressedViolations().size(), r.getProcessingErrors().size(), r.getConfigurationErrors().size());
        if (!r.getViolations().isEmpty()) {
            LOGGER.debug("Violations: {}", r.getViolations());
        }
        if (!r.getSuppressedViolations().isEmpty()) {
            LOGGER.debug("SuppressedViolations: {}", r.getSuppressedViolations());
        }
        if (!r.getProcessingErrors().isEmpty()) {
            LOGGER.debug("ProcessingErrors: {}", r.getProcessingErrors());
        }
        if (!r.getConfigurationErrors().isEmpty()) {
            LOGGER.debug("ConfigurationErrors: {}", r.getConfigurationErrors());
        }
    }

    private Iterable<InputFile> hasFiles(Type fileType, String languageKey) {
        final FilePredicates predicates = fs.predicates();
        return fs.inputFiles(
                predicates.and(
                        predicates.hasLanguage(languageKey),
                        predicates.hasType(fileType)
                )
        );
    }

    private Optional<Report> executeRules(PmdTemplate pmdFactory, Iterable<InputFile> files, String repositoryKey) {
        if (!files.iterator().hasNext()) {
            // Nothing to analyze
            LOGGER.debug("No files to analyze for {}", repositoryKey);
            return Optional.empty();
        }

        final RuleSet ruleSet = createRuleSet(repositoryKey);

        if (ruleSet.size() < 1) {
            // No rule
            LOGGER.debug("No rules to apply for {}", repositoryKey);
            return Optional.empty();
        }

        LOGGER.debug("Found {} rules for {}", ruleSet.size(), repositoryKey);
        return Optional.ofNullable(pmdFactory.process(files, ruleSet));
    }

    private RuleSet createRuleSet(String repositoryKey) {
        final String rulesXml = dumpXml(rulesProfile, repositoryKey);
        final File ruleSetFile = pmdConfiguration.dumpXmlRuleSet(repositoryKey, rulesXml);
        final String ruleSetFilePath = ruleSetFile.getAbsolutePath();

        try {
            return new RuleSetLoader()
                    .loadFromResource(ruleSetFilePath);
        } catch (RuleSetLoadException e) {
            throw new IllegalStateException(e);
        }
    }

    private String dumpXml(ActiveRules rulesProfile, String repositoryKey) {
        final StringWriter writer = new StringWriter();
        final PmdRuleSet ruleSet = PmdRuleSets.from(rulesProfile, repositoryKey);
        ruleSet.writeTo(writer);

        return writer.toString();
    }

    PmdTemplate createPmdTemplate(URLClassLoader classLoader) {
        return PmdTemplate.create(getSourceVersion(), classLoader, fs.encoding());
    }

    /**
     * @return A classloader for PMD that contains all dependencies of the project that shall be analyzed.
     */
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

    private String getSourceVersion() {
        String reqJavaVersion = settings.get(PmdConstants.JAVA_SOURCE_VERSION).orElse(PmdConstants.JAVA_SOURCE_VERSION_DEFAULT_VALUE);
        String bareReqJavaVersion = reqJavaVersion;
        if (reqJavaVersion.endsWith("-preview")) {
            bareReqJavaVersion = reqJavaVersion.substring(0, reqJavaVersion.indexOf("-preview"));
        }
        String effectiveJavaVersion = bareReqJavaVersion;
        if (Float.parseFloat(bareReqJavaVersion) >= Float.parseFloat(PmdConstants.JAVA_SOURCE_MINIMUM_UNSUPPORTED_VALUE)) {
            effectiveJavaVersion = PmdConstants.JAVA_SOURCE_MAXIMUM_SUPPORTED_VALUE;
            LOGGER.warn("Requested Java version {} ('{}') is not supported by PMD. Using maximum supported version: {}.",
                    reqJavaVersion, PmdConstants.JAVA_SOURCE_VERSION, PmdConstants.JAVA_SOURCE_MAXIMUM_SUPPORTED_VALUE);
        }
        return effectiveJavaVersion;
    }

}
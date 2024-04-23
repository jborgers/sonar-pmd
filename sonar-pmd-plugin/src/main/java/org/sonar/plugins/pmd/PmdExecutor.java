/*
 * SonarQube PMD Plugin
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

import net.sourceforge.pmd.*;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.log.Profiler;
import org.sonar.plugins.java.api.JavaResourceLocator;
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

@ScannerSide
public class PmdExecutor {

    private static final Logger LOGGER = Loggers.get(PmdExecutor.class);

    private final FileSystem fs;
    private final ActiveRules rulesProfile;
    private final PmdConfiguration pmdConfiguration;
    private final JavaResourceLocator javaResourceLocator;
    private final Configuration settings;

    public PmdExecutor(FileSystem fileSystem, ActiveRules rulesProfile,
                       PmdConfiguration pmdConfiguration, JavaResourceLocator javaResourceLocator, Configuration settings) {
        this.fs = fileSystem;
        this.rulesProfile = rulesProfile;
        this.pmdConfiguration = pmdConfiguration;
        this.javaResourceLocator = javaResourceLocator;
        this.settings = settings;
    }

    public Report execute() {
        final Profiler profiler = Profiler.create(LOGGER).startInfo("Execute PMD " + PMDVersion.VERSION);
        final ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();

        try (URLClassLoader classLoader = createClassloader()) {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return executePmd(classLoader);
        } catch (IOException e) {
            LOGGER.error("Failed to close URLClassLoader.", e);
        } finally {
            Thread.currentThread().setContextClassLoader(initialClassLoader);
            profiler.stopInfo();
        }

        return null;
    }

    private Report executePmd(URLClassLoader classLoader) {

        final PmdTemplate pmdFactory = createPmdTemplate(classLoader);
        final Optional<Report> mainReport = executeRules(pmdFactory, javaFiles(Type.MAIN), PmdConstants.REPOSITORY_KEY);
        final Optional<Report> testReport = executeRules(pmdFactory, javaFiles(Type.TEST), PmdConstants.TEST_REPOSITORY_KEY);

        Report report = mainReport
                .orElse(
                        testReport.orElse(new Report()) // TODO solve deprecation, difficult now
                );

        if (mainReport.isPresent() && testReport.isPresent()) {
            report = mainReport.get().union(testReport.get());
        }

        pmdConfiguration.dumpXmlReport(report);

        return report;
    }

    private Iterable<InputFile> javaFiles(Type fileType) {
        final FilePredicates predicates = fs.predicates();
        return fs.inputFiles(
                predicates.and(
                        predicates.hasLanguage(PmdConstants.LANGUAGE_KEY),
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
        Collection<File> classpathElements = javaResourceLocator.classpath();
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
            LOGGER.warn("Requested Java version " + reqJavaVersion + " ('" + PmdConstants.JAVA_SOURCE_VERSION + "') is not supported by PMD. Using maximum supported version: " + PmdConstants.JAVA_SOURCE_MAXIMUM_SUPPORTED_VALUE + ".");
        }
        return effectiveJavaVersion;
    }

}

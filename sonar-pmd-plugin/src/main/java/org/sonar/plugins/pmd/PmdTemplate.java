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

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.apex.ApexLanguageModule;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.kotlin.KotlinLanguageModule;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.renderers.EmptyRenderer;
import net.sourceforge.pmd.reporting.Report;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;

public class PmdTemplate {

    private static final Logger LOG = Loggers.get(PmdTemplate.class);
    private static final Map<String, String> JAVA_VERSIONS = prepareVersions();

    private static Map<String, String> prepareVersions() {
        final Map<String, String> versions = new HashMap<>();
        versions.put("8", "1.8");
        versions.put("1.9", "9");
        versions.put("1.10", "10");
        versions.put("1.11", "11");
        versions.put("1.12", "12");
        versions.put("1.13", "13");
        versions.put("1.14", "14");
        versions.put("1.15", "15");
        versions.put("1.16", "16");
        versions.put("1.17", "17");
        versions.put("1.18", "18");
        versions.put("1.19", "19");
        versions.put("1.20", "20");
        versions.put("1.21", "21");
        versions.put("1.22", "22");
        versions.put("1.23", "23");
        versions.put("1.24", "24");
        versions.put("1.24-preview", "24-preview");
        return versions;
    }

    private final PMDConfiguration configuration;

    PmdTemplate(PMDConfiguration configuration) {
        this.configuration = configuration;
    }

    public static PmdTemplate create(String javaVersion, ClassLoader classloader, Charset charset) {
        PMDConfiguration configuration = new PMDConfiguration();

        // Set Java as the default language version
        LanguageVersion javaLanguageVersion = languageVersion(PmdConstants.LANGUAGE_JAVA_KEY, javaVersion);
        configuration.setDefaultLanguageVersion(javaLanguageVersion);
        LOG.info("Set default language version to Java: " + javaLanguageVersion.getName());

        configuration.setClassLoader(classloader);
        configuration.setSourceEncoding(charset);
        configuration.setFailOnViolation(false);
        configuration.setIgnoreIncrementalAnalysis(true);
        configuration.setReportFormat(EmptyRenderer.NAME);

        return new PmdTemplate(configuration);
    }

    static LanguageVersion languageVersion(String languageKey, String version) {
        if (PmdConstants.LANGUAGE_JAVA_KEY.equals(languageKey)) {
            String normalizedVersion = normalize(version);
            LanguageVersion languageVersion = new JavaLanguageModule().getVersion(normalizedVersion);
            if (languageVersion == null) {
                throw new IllegalArgumentException("Unsupported Java version for PMD: " + normalizedVersion);
            }
            LOG.info("Java version: " + normalizedVersion);
            return languageVersion;
        } else if (PmdConstants.LANGUAGE_APEX_KEY.equals(languageKey)) {
            LanguageVersion languageVersion = new ApexLanguageModule().getDefaultVersion();
            LOG.info("Using Apex default version");
            return languageVersion;
        } else if (PmdConstants.LANGUAGE_KOTLIN_KEY.equals(languageKey)) {
            LanguageVersion languageVersion = new KotlinLanguageModule().getDefaultVersion();
            LOG.info("Using Kotlin default version");
            return languageVersion;
        }

        // Default to Java
        return new JavaLanguageModule().getDefaultVersion();
    }

    private static String normalize(String version) {
        return JAVA_VERSIONS.getOrDefault(version, version);
    }

    PMDConfiguration configuration() {
        return configuration;
    }

    public Report process(Iterable<InputFile> files, RuleSet ruleset) {
        try (PmdAnalysis pmd = PmdAnalysis.create(configuration)) {
            pmd.addRuleSet(ruleset);

            // Group files by language
            Map<String, List<InputFile>> filesByLanguage = new HashMap<>();
            for (InputFile file : files) {
                String language = file.language();
                if (language == null) {
                    // Try to determine language from file extension
                    String filename = file.filename();
                    if (filename.endsWith(".cls") || filename.endsWith(".trigger")) {
                        language = PmdConstants.LANGUAGE_APEX_KEY;
                    } else if (filename.endsWith(".kt") || filename.endsWith(".kts")) {
                        language = PmdConstants.LANGUAGE_KOTLIN_KEY;
                    } else {
                        // Default to Java
                        language = PmdConstants.LANGUAGE_JAVA_KEY;
                    }
                }

                filesByLanguage.computeIfAbsent(language, k -> new ArrayList<>()).add(file);
            }

            // Process files by language
            for (Map.Entry<String, List<InputFile>> entry : filesByLanguage.entrySet()) {
                String language = entry.getKey();
                List<InputFile> languageFiles = entry.getValue();

                if (!languageFiles.isEmpty()) {
                    LOG.info("Processing {} files with language: {}", languageFiles.size(), language);

                    // Set the appropriate language version for this batch of files
                    if (PmdConstants.LANGUAGE_APEX_KEY.equals(language)) {
                        LanguageVersion apexVersion = new ApexLanguageModule().getDefaultVersion();
                        configuration.setDefaultLanguageVersion(apexVersion);
                        LOG.info("Set language version to Apex: {}", apexVersion.getName());
                    } else if (PmdConstants.LANGUAGE_KOTLIN_KEY.equals(language)) {
                        LanguageVersion kotlinVersion = new KotlinLanguageModule().getDefaultVersion();
                        configuration.setDefaultLanguageVersion(kotlinVersion);
                        LOG.info("Set language version to Kotlin: {}", kotlinVersion.getName());
                    } else {
                        // Default to Java
                        LanguageVersion javaVersion = languageVersion(PmdConstants.LANGUAGE_JAVA_KEY, "24");
                        configuration.setDefaultLanguageVersion(javaVersion);
                        LOG.info("Set language version to Java: {}", javaVersion.getName());
                    }

                    // Add files to PMD
                    for (InputFile file : languageFiles) {
                        pmd.files().addFile(Paths.get(file.uri()));
                    }
                }
            }

            return pmd.performAnalysisAndCollectReport();
        }
    }
}

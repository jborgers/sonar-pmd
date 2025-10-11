/*
 * Shared PMD template moved to lib to be reused by plugins.
 */
package org.sonar.plugins.pmd;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
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
import java.util.*;

public class PmdTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(PmdTemplate.class);
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
        versions.put("1.25", "25");
        versions.put("1.25-preview", "25-preview");
        return versions;
    }

    private final PMDConfiguration configuration;

    PmdTemplate(PMDConfiguration configuration) {
        this.configuration = configuration;
    }

    public static PmdTemplate create(String javaVersion, ClassLoader classloader, Charset charset) {
        PMDConfiguration configuration = new PMDConfiguration();

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
            Language apex = LanguageRegistry.PMD.getLanguageById("apex");
            if (apex != null) {
                LanguageVersion languageVersion = apex.getDefaultVersion();
                LOG.info("Using Apex default version");
                return languageVersion;
            }
            LOG.warn("Apex language module not found on classpath; falling back to Java default version");
            return new JavaLanguageModule().getDefaultVersion();
        } else if (PmdConstants.LANGUAGE_KOTLIN_KEY.equals(languageKey)) {
            Language kotlin = LanguageRegistry.PMD.getLanguageById("kotlin");
            if (kotlin != null) {
                LanguageVersion languageVersion = kotlin.getDefaultVersion();
                LOG.info("Using Kotlin default version");
                return languageVersion;
            }
            LOG.warn("Kotlin language module not found on classpath; falling back to Java default version");
            return new JavaLanguageModule().getDefaultVersion();
        }

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

            Map<String, List<InputFile>> filesByLanguage = new HashMap<>();
            for (InputFile file : files) {
                String language = file.language();
                if (language == null) {
                    String filename = file.filename();
                    if (filename.endsWith(".cls") || filename.endsWith(".trigger")) {
                        language = PmdConstants.LANGUAGE_APEX_KEY;
                    } else if (filename.endsWith(".kt") || filename.endsWith(".kts")) {
                        language = PmdConstants.LANGUAGE_KOTLIN_KEY;
                    } else {
                        language = PmdConstants.LANGUAGE_JAVA_KEY;
                    }
                }

                filesByLanguage.computeIfAbsent(language, k -> new ArrayList<>()).add(file);
            }

            for (Map.Entry<String, List<InputFile>> entry : filesByLanguage.entrySet()) {
                String language = entry.getKey();
                List<InputFile> languageFiles = entry.getValue();

                if (!languageFiles.isEmpty()) {
                    LOG.info("Processing {} files with language: {}", languageFiles.size(), language);

                    if (PmdConstants.LANGUAGE_APEX_KEY.equals(language)) {
                        Language apex = LanguageRegistry.PMD.getLanguageById("apex");
                        if (apex != null) {
                            LanguageVersion apexVersion = apex.getDefaultVersion();
                            configuration.setDefaultLanguageVersion(apexVersion);
                            LOG.info("Set language version to Apex: {}", apexVersion.getName());
                        } else {
                            LOG.warn("Apex language module not found on classpath; keeping current default version");
                        }
                    } else if (PmdConstants.LANGUAGE_KOTLIN_KEY.equals(language)) {
                        Language kotlin = LanguageRegistry.PMD.getLanguageById("kotlin");
                        if (kotlin != null) {
                            LanguageVersion kotlinVersion = kotlin.getDefaultVersion();
                            configuration.setDefaultLanguageVersion(kotlinVersion);
                            LOG.info("Set language version to Kotlin: {}", kotlinVersion.getName());
                        } else {
                            LOG.warn("Kotlin language module not found on classpath; keeping current default version");
                        }
                    } else {
                        LanguageVersion javaVersion = languageVersion(PmdConstants.LANGUAGE_JAVA_KEY, PmdConstants.JAVA_SOURCE_VERSION_DEFAULT_VALUE);
                        configuration.setDefaultLanguageVersion(javaVersion);
                        LOG.info("Set language version to Java: {}", javaVersion.getName());
                    }

                    for (InputFile file : languageFiles) {
                        pmd.files().addFile(Paths.get(file.uri()));
                    }
                }
            }

            return pmd.performAnalysisAndCollectReport();
        }
    }
}

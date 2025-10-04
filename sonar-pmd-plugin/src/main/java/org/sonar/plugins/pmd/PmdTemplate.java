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
        configuration.setDefaultLanguageVersion(languageVersion(javaVersion));
        configuration.setClassLoader(classloader);
        configuration.setSourceEncoding(charset);
        configuration.setFailOnViolation(false);
        configuration.setIgnoreIncrementalAnalysis(true);
        configuration.setReportFormat(EmptyRenderer.NAME);

        return new PmdTemplate(configuration);
    }

    static LanguageVersion languageVersion(String javaVersion) {
        String version = normalize(javaVersion);
        LanguageVersion languageVersion = new JavaLanguageModule().getVersion(version);
        if (languageVersion == null) {
            throw new IllegalArgumentException("Unsupported Java version for PMD: " + version);
        }
        LOG.info("Java version: {}", version);
        return languageVersion;
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
            for (InputFile file: files) {
                pmd.files().addFile(Paths.get(file.uri()));
            }
            return pmd.performAnalysisAndCollectReport();
        }
    }
}

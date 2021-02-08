/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2021 SonarSource SA and others
 * mailto:jens AT gerdes DOT digital
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

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.renderers.EmptyRenderer;
import net.sourceforge.pmd.util.datasource.DataSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.nio.charset.Charset;
import java.util.*;

public class PmdTemplate {

    private static final Logger LOG = Loggers.get(PmdTemplate.class);
    private static final Map<String, String> JAVA_VERSIONS = prepareVersions();

    private static Map<String, String> prepareVersions() {
        final Map<String, String> versions = new HashMap<>();
        versions.put("1.1", "1.3");
        versions.put("1.2", "1.3");
        versions.put("5", "1.5");
        versions.put("6", "1.6");
        versions.put("7", "1.7");
        versions.put("8", "1.8");
        versions.put("1.9", "9");
        versions.put("1.10", "10");
        versions.put("1.11", "11");

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
        configuration.setSourceEncoding(charset.name());
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
        LOG.info("Java version: " + version);
        return languageVersion;
    }

    private static String normalize(String version) {
        return JAVA_VERSIONS.getOrDefault(version, version);
    }

    PMDConfiguration configuration() {
        return configuration;
    }

    private Collection<DataSource> toDataSources(Iterable<InputFile> files) {
        final Collection<DataSource> dataSources = new ArrayList<>();

        files.forEach(file -> dataSources.add(new ProjectDataSource(file)));

        return dataSources;
    }

    public Report process(Iterable<InputFile> files, RuleSet ruleset) {
        return PMD.processFiles(
                configuration,
                Collections.singletonList(ruleset),
                toDataSources(files),
                Collections.emptyList()
        );
    }
}

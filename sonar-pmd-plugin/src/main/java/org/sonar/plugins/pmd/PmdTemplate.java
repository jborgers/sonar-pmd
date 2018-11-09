/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PMDException;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.SourceCodeProcessor;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

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
        versions.put("9", "9");
        versions.put("10", "10");
        versions.put("11", "11");

        return versions;
    }

    private SourceCodeProcessor processor;
    private PMDConfiguration configuration;

    PmdTemplate(PMDConfiguration configuration, SourceCodeProcessor processor) {
        this.configuration = configuration;
        this.processor = processor;
    }

    public static PmdTemplate create(String javaVersion, ClassLoader classloader, Charset charset) {
        PMDConfiguration configuration = new PMDConfiguration();
        configuration.setDefaultLanguageVersion(languageVersion(javaVersion));
        configuration.setClassLoader(classloader);
        configuration.setSourceEncoding(charset.name());
        SourceCodeProcessor processor = new SourceCodeProcessor(configuration);
        return new PmdTemplate(configuration, processor);
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

    public void process(InputFile file, RuleSets rulesets, RuleContext ruleContext) {
        ruleContext.setSourceCodeFilename(file.uri().toString());

        try (InputStream inputStream = file.inputStream()) {
            processor.processSourceCode(inputStream, rulesets, ruleContext);
        } catch (RuntimeException | IOException | PMDException e) {
            LOG.error("Fail to execute PMD. Following file is ignored: " + file, e);
        }
    }
}

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

import net.sourceforge.pmd.reporting.Report;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.rule.RuleScope;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

class PmdConfigurationTest {

    private static final Pattern PMD_XML_PATTERN = Pattern.compile("^<\\?xml version=\"1\\.0\" encoding=\"UTF-8\"\\?><pmd xmlns=\"http://pmd\\.sourceforge\\.net/report/2\\.0\\.0\" xmlns:xsi=\"http://www\\.w3\\.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://pmd\\.sourceforge\\.net/report/2\\.0\\.0 https://pmd\\.github\\.io/schema/report_2_0_0.xsd\" version=\"7\\.\\d+\\.\\d+\".*></pmd>$");
    private static final File WORK_DIR = new File("test-work-dir");

    private final FileSystem fs = mock(FileSystem.class);
    private PmdConfiguration configuration;
    private MapSettings settings;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeAll
    static void createTempDir() {
        deleteTempDir();
        WORK_DIR.mkdir();
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    @AfterAll
    static void deleteTempDir() {
        if (WORK_DIR.exists()) {
            for (File file : WORK_DIR.listFiles()) {
                file.delete();
            }
            WORK_DIR.delete();
        }
    }

    @BeforeEach
    void setUpPmdConfiguration() {
        settings = new MapSettings();
        configuration = new PmdConfiguration(fs, settings.asConfig());
    }

    @Test
    void should_dump_xml_rule_set() throws IOException {
        when(fs.workDir()).thenReturn(WORK_DIR);

        File rulesFile = configuration.dumpXmlRuleSet("pmd", "<rules>", RuleScope.TEST);

        assertThat(rulesFile).isEqualTo(new File(WORK_DIR, "pmd.xml"));
        assertThat(Files.readAllLines(rulesFile.toPath(), StandardCharsets.UTF_8)).containsExactly("<rules>");
    }

    @Test
    void should_fail_to_dump_xml_rule_set() {
        when(fs.workDir()).thenReturn(new File("xxx"));

        final Throwable thrown = catchThrowable(() -> configuration.dumpXmlRuleSet("pmd", "<xml>", RuleScope.MAIN));

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Fail to save the PMD configuration");
    }

    @Test
    void should_dump_xml_report() throws IOException {
        when(fs.workDir()).thenReturn(WORK_DIR);

        settings.setProperty(PmdConfiguration.PROPERTY_GENERATE_XML, true);
        Path reportFile = configuration.dumpXmlReport(Report.buildReport(v -> {}));

        assertThat(reportFile.toFile()).isEqualTo(new File(WORK_DIR, "pmd-result.xml"));
        String pmdResultXML = String.join("", Files.readAllLines(reportFile, StandardCharsets.UTF_8));

        assertThat(pmdResultXML)
                .matches(PMD_XML_PATTERN);
    }

    @Test
    void should_fail_to_dump_xml_report() {
        when(fs.workDir()).thenReturn(new File("xxx"));

        settings.setProperty(PmdConfiguration.PROPERTY_GENERATE_XML, true);

        final Throwable thrown = catchThrowable(() -> configuration.dumpXmlReport(Report.buildReport(v -> {})));

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Fail to save the PMD report");
    }

    @Test
    void should_ignore_xml_report_when_property_is_not_set() {
        Path reportFile = configuration.dumpXmlReport(Report.buildReport(v -> {}));

        assertThat(reportFile).isNull();
        verifyNoMoreInteractions(fs);
    }
}

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

import net.sourceforge.pmd.reporting.FileAnalysisListener;
import net.sourceforge.pmd.reporting.Report;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.Configuration;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * PMD executor for Apex files.
 */
@ScannerSide
public class PmdApexExecutor extends AbstractPmdExecutor {

    public PmdApexExecutor(FileSystem fileSystem, ActiveRules rulesProfile,
                       PmdConfiguration pmdConfiguration, Configuration settings) {
        super(fileSystem, rulesProfile, pmdConfiguration, settings);
    }

    @Override
    protected String getStartMessage() {
        return "Execute PMD {} for Apex";
    }

    @Override
    protected String getEndMessage() {
        return "Execute PMD {} for Apex (done) | time={}ms";
    }

    @Override
    protected Report executePmd(URLClassLoader classLoader) {
        final PmdTemplate pmdFactory = createPmdTemplate(classLoader);
        final Optional<Report> apexMainReport = executeRules(pmdFactory, hasFiles(Type.MAIN, PmdConstants.LANGUAGE_APEX_KEY), PmdConstants.MAIN_APEX_REPOSITORY_KEY);
        final Optional<Report> apexTestReport = executeRules(pmdFactory, hasFiles(Type.TEST, PmdConstants.LANGUAGE_APEX_KEY), PmdConstants.MAIN_APEX_REPOSITORY_KEY);

        if (LOGGER.isDebugEnabled()) {
            apexMainReport.ifPresent(this::writeDebugLine);
            apexTestReport.ifPresent(this::writeDebugLine);
        }

        Consumer<FileAnalysisListener> fileAnalysisListenerConsumer = AbstractPmdExecutor::accept;

        Report unionReport = Report.buildReport(fileAnalysisListenerConsumer);
        unionReport = apexMainReport.map(unionReport::union).orElse(unionReport);
        unionReport = apexTestReport.map(unionReport::union).orElse(unionReport);

        pmdConfiguration.dumpXmlReport(unionReport);

        return unionReport;
    }

    /**
     * @return A classloader for PMD that contains no additional dependencies.
     * For Apex projects, we don't need the project's classpath.
     */
    @Override
    protected URLClassLoader createClassloader() {
        // Create an empty URLClassLoader
        return new URLClassLoader(new URL[0]);
    }
}
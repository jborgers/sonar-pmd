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
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.pmd.util.ClassLoaderUtils;

import java.net.URLClassLoader;

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
        return executeLanguage(classLoader, PmdConstants.LANGUAGE_APEX_KEY, PmdConstants.MAIN_APEX_REPOSITORY_KEY);
    }

    /**
     * @return A classloader for PMD that contains no additional dependencies.
     * For Apex projects, we don't need the project's classpath.
     */
    @Override
    protected URLClassLoader createClassloader() {
        return ClassLoaderUtils.empty();
    }
}
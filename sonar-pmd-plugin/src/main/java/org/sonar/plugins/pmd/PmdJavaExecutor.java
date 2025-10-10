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

import java.io.File;
import java.net.URLClassLoader;
import java.util.Collection;

/**
 * PMD executor for Java files.
 */
@ScannerSide
public class PmdJavaExecutor extends AbstractPmdExecutor {

    private final ClasspathProvider classpathProvider;

    public PmdJavaExecutor(FileSystem fileSystem, ActiveRules rulesProfile,
                       PmdConfiguration pmdConfiguration, ClasspathProvider classpathProvider, Configuration settings) {
        super(fileSystem, rulesProfile, pmdConfiguration, settings);
        this.classpathProvider = classpathProvider;
    }

    @Override
    protected String getStartMessage() {
        return "Execute PMD {}";
    }

    @Override
    protected String getEndMessage() {
        return "Execute PMD {} (done) | time={}ms";
    }

    @Override
    protected Report executePmd(URLClassLoader classLoader) {
        return executeLanguage(classLoader, PmdConstants.LANGUAGE_JAVA_KEY, PmdConstants.MAIN_JAVA_REPOSITORY_KEY);
    }

    /**
     * @return A classloader for PMD that contains all dependencies of the project that shall be analyzed.
     */
    @Override
    protected URLClassLoader createClassloader() {
        Collection<File> classpathElements = classpathProvider.classpath();
        return ClassLoaderUtils.fromClasspath(classpathElements);
    }
}
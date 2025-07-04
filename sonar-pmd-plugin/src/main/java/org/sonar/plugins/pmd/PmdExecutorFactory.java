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

import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.java.api.JavaResourceLocator;

/**
 * Factory for creating PmdExecutor instances.
 * This class handles the case where JavaResourceLocator is not available
 * (e.g., when analyzing non-Java projects).
 */
@ScannerSide
public class PmdExecutorFactory {

    private static final Logger LOGGER = Loggers.get(PmdExecutorFactory.class);

    private final FileSystem fileSystem;
    private final ActiveRules activeRules;
    private final PmdConfiguration pmdConfiguration;
    private final Configuration settings;
    private final JavaResourceLocator javaResourceLocator;

    /**
     * Constructor with all dependencies including JavaResourceLocator.
     * This constructor will be used by Sonar's dependency injection system
     * when JavaResourceLocator is available.
     */
    public PmdExecutorFactory(FileSystem fileSystem, ActiveRules activeRules,
                             PmdConfiguration pmdConfiguration, Configuration settings,
                             JavaResourceLocator javaResourceLocator) {
        this.fileSystem = fileSystem;
        this.activeRules = activeRules;
        this.pmdConfiguration = pmdConfiguration;
        this.settings = settings;
        this.javaResourceLocator = javaResourceLocator;
        LOGGER.debug("PmdExecutorFactory created with JavaResourceLocator");
    }

    /**
     * Constructor without JavaResourceLocator.
     * This constructor will be used by Sonar's dependency injection system
     * when JavaResourceLocator is not available.
     */
    public PmdExecutorFactory(FileSystem fileSystem, ActiveRules activeRules,
                             PmdConfiguration pmdConfiguration, Configuration settings) {
        this.fileSystem = fileSystem;
        this.activeRules = activeRules;
        this.pmdConfiguration = pmdConfiguration;
        this.settings = settings;
        this.javaResourceLocator = null;
        LOGGER.debug("PmdExecutorFactory created without JavaResourceLocator");
    }

    /**
     * Creates a PmdExecutor instance.
     * If JavaResourceLocator is available, it will be passed to the PmdExecutor.
     * Otherwise, a PmdExecutor without JavaResourceLocator will be created.
     *
     * @return A PmdExecutor instance
     */
    public PmdExecutor create() {
        if (javaResourceLocator != null) {
            LOGGER.debug("Creating PmdExecutor with JavaResourceLocator");
            return new PmdExecutor(fileSystem, activeRules, pmdConfiguration, javaResourceLocator, settings);
        } else {
            LOGGER.debug("Creating PmdExecutor without JavaResourceLocator");
            return new PmdExecutor(fileSystem, activeRules, pmdConfiguration, settings);
        }
    }
}
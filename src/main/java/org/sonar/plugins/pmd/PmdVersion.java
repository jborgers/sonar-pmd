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
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public final class PmdVersion {

    private static final Logger LOG = Loggers.get(PmdVersion.class);
    private static final String PROPERTIES_PATH = "/org/sonar/plugins/pmd/pmd-plugin.properties";

    private PmdVersion() {
        // Static utility class
    }

    public static String getVersion() {

        try (InputStream input = PmdVersion.class.getResourceAsStream(PROPERTIES_PATH)) {
            final Properties properties = new Properties();
            properties.load(input);

            return properties.getProperty("pmd.version", "");
        } catch (IOException e) {
            LOG.warn("Failed to parse PMD Version from properties file.", e);
            return "";
        }
    }
}

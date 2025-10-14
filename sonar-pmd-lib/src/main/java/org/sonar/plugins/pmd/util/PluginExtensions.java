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
package org.sonar.plugins.pmd.util;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;

import java.util.Arrays;
import java.util.List;

/**
 * Utility methods to help registering extensions and common properties for Sonar-PMD plugins.
 *
 * This class lives in sonar-pmd-lib so both the main plugin and the integration-test extension plugin
 * can reuse the same utility and keep their Plugin implementations concise.
 */
public final class PluginExtensions {

    private PluginExtensions() {
        // utility
    }

    /**
     * Adds the given extensions to the plugin context.
     *
     * @param context    The SonarQube plugin context
     * @param extensions The extensions to add (classes or instances)
     */
    public static void addExtensions(Plugin.Context context, Object... extensions) {
        if (extensions == null || extensions.length == 0) {
            return;
        }
        List<Object> list = Arrays.asList(extensions);
        context.addExtensions(list);
    }

    /**
     * Creates the hidden PropertyDefinition for the XML report switch used by PMD execution.
     * The property key should be {@code org.sonar.plugins.pmd.PmdConfiguration#PROPERTY_GENERATE_XML}.
     *
     * @param propertyKey The configuration property key
     * @return PropertyDefinition ready to be added to the Plugin context
     */
    public static PropertyDefinition xmlReportProperty(String propertyKey) {
        return PropertyDefinition.builder(propertyKey)
                .defaultValue("false")
                .name("Generate XML Report")
                .hidden()
                .build();
    }
}

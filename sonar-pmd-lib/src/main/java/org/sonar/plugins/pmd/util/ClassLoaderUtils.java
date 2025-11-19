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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility methods to construct URLClassLoaders in a consistent and safe way.
 * Located in sonar-pmd-lib for reuse by both the core plugin and extension examples.
 */
public final class ClassLoaderUtils {

    private ClassLoaderUtils() {
        // utility class
    }

    /**
     * Create an URLClassLoader from a collection of classpath elements (directories or jars).
     *
     * @param classpathElements Collection of files to be added to the classloader
     * @return URLClassLoader containing all provided elements
     * @throws IllegalStateException if any classpath element cannot be converted to a URL
     */
    public static URLClassLoader fromClasspath(Collection<File> classpathElements) {
        List<URL> urls = new ArrayList<>();
        if (classpathElements != null) {
            for (File file : classpathElements) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new IllegalStateException("Failed to create the project classloader. Classpath element is invalid: " + file, e);
                }
            }
        }
        return new URLClassLoader(urls.toArray(new URL[0]));
    }

    /**
     * Create an empty URLClassLoader.
     */
    public static URLClassLoader empty() {
        return new URLClassLoader(new URL[0]);
    }
}

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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PmdTestUtils {

    public static String getResourceContent(String path) {

        try {
            final URI resource = PmdTestUtils.class.getResource(path).toURI();
            return new String(Files.readAllBytes(Paths.get(resource)), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Given Path " + path + " can not be resolved to a valid URI.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Requested Path " + path + " seems to not be available.", e);
        }
    }
}

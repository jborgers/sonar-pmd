/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2021 SonarSource SA and others
 * mailto:jens AT gerdes DOT digital
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

import net.sourceforge.pmd.util.datasource.DataSource;
import org.sonar.api.batch.fs.InputFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class ProjectDataSource implements DataSource {

    private final InputFile inputFile;

    public ProjectDataSource(InputFile inputFile) {
        this.inputFile = inputFile;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputFile.inputStream();
    }

    @Override
    public String getNiceFileName(boolean shortNames, String inputFileName) {
        return Paths.get(inputFile.uri())
                .toAbsolutePath()
                .toString();
    }

    @Override
    public void close() throws IOException {
        // empty default implementation
    }
}

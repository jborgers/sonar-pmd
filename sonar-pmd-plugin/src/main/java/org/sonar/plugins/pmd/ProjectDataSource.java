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

import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.lang.document.TextFileContent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.sonar.api.batch.fs.InputFile;

import java.io.IOException;

public class ProjectDataSource implements TextFile {

    private final TextFileContent textFileContent;

    public ProjectDataSource(InputFile inputFile) {
        try {
            this.textFileContent =
                    TextFileContent.fromInputStream(inputFile.inputStream(), inputFile.charset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public InputStream getInputStream() throws IOException {
//        return inputFile.inputStream();
//    }
//
//    @Override
//    public String getNiceFileName(boolean shortNames, String inputFileName) {
//        return Paths.get(inputFile.uri())
//                .toAbsolutePath()
//                .toString();
//    }

    @Override
    public @NonNull LanguageVersion getLanguageVersion() {
        return null;
    }

    @Override
    public FileId getFileId() {
        return null;
    }

    @Override
    public TextFileContent readContents() throws IOException {
        return textFileContent;
    }

    @Override
    public void close() throws IOException {
        // empty default implementation
    }
}

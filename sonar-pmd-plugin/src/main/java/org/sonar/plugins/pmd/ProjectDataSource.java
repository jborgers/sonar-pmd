package org.sonar.plugins.pmd;

import net.sourceforge.pmd.util.datasource.DataSource;
import org.sonar.api.batch.fs.InputFile;

import java.io.IOException;
import java.io.InputStream;

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
        return inputFile.filename();
    }

    @Override
    public void close() throws IOException {
        // empty default implementation
    }
}

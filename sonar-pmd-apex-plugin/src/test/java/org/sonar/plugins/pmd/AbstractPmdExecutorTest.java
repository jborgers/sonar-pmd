/*
 * SonarQube PMD7 Plugin - Apex module tests
 */
package org.sonar.plugins.pmd;

import net.sourceforge.pmd.lang.rule.RuleSetLoadException;
import net.sourceforge.pmd.reporting.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.rule.RuleScope;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractPmdExecutorTest {

    protected final DefaultFileSystem fileSystem = new DefaultFileSystem(new File("."));
    protected final ActiveRules activeRules = mock(ActiveRules.class);
    protected final PmdConfiguration pmdConfiguration = mock(PmdConfiguration.class);
    protected final PmdTemplate pmdTemplate = mock(PmdTemplate.class);
    protected final MapSettings settings = new MapSettings();

    protected AbstractPmdExecutor pmdExecutor;

    protected static DefaultInputFile fileApex(String path, Type type) {
        return TestInputFileBuilder.create("", path)
                .setType(type)
                .setLanguage(PmdConstants.LANGUAGE_APEX_KEY)
                .build();
    }

    @BeforeEach
    void setUpAbstractTest() {
        fileSystem.setEncoding(StandardCharsets.UTF_8);
        settings.setProperty(PmdConstants.JAVA_SOURCE_VERSION, "1.8");
    }

    @Test
    void whenNoFilesToAnalyzeThenExecutionSucceedsWithBlankReport() {
        final Report result = pmdExecutor.execute();
        assertThat(result).isNotNull();
        assertThat(result.getViolations()).isEmpty();
        assertThat(result.getProcessingErrors()).isEmpty();
    }

    @Test
    void unknown_pmd_ruleset() {
        when(pmdConfiguration.dumpXmlRuleSet(anyString(), anyString(), ArgumentMatchers.any(RuleScope.class))).thenReturn(new File("unknown"));

        DefaultInputFile srcFile = getAppropriateInputFileForTest();
        fileSystem.add(srcFile);

        final Throwable thrown = catchThrowable(() -> pmdExecutor.execute());

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(RuleSetLoadException.class);
    }

    protected abstract DefaultInputFile getAppropriateInputFileForTest();

    protected void setupPmdRuleSet(String repositoryKey, String profileFileName) {
        final Path sourcePath = Paths.get("src/test/resources/org/sonar/plugins/pmd/").resolve(profileFileName);
        when(pmdConfiguration.dumpXmlRuleSet(eq(repositoryKey), anyString(), ArgumentMatchers.any(RuleScope.class))).thenReturn(sourcePath.toFile());
    }
}

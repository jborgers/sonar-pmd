/*
 * SonarQube PMD7 Plugin - Apex module
 */
package org.sonar.plugins.pmd;

import net.sourceforge.pmd.reporting.Report;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.pmd.util.ClassLoaderUtils;

import java.net.URLClassLoader;

/**
 * PMD executor for Apex files.
 */
@ScannerSide
public class PmdApexExecutor extends AbstractPmdExecutor {

    public PmdApexExecutor(FileSystem fileSystem, ActiveRules rulesProfile,
                       PmdConfiguration pmdConfiguration, Configuration settings) {
        super(fileSystem, rulesProfile, pmdConfiguration, settings);
    }

    @Override
    protected String getStartMessage() {
        return "Execute PMD {} for Apex";
    }

    @Override
    protected String getEndMessage() {
        return "Execute PMD {} for Apex (done) | time={}ms";
    }

    @Override
    protected Report executePmd(URLClassLoader classLoader) {
        return executeLanguage(classLoader, PmdConstants.LANGUAGE_APEX_KEY, PmdConstants.MAIN_APEX_REPOSITORY_KEY);
    }

    /**
     * @return A classloader for PMD that contains no additional dependencies.
     * For Apex projects, we don't need the project's classpath.
     */
    @Override
    protected URLClassLoader createClassloader() {
        return ClassLoaderUtils.empty();
    }
}

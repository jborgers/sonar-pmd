/*
 * Shared abstract PMD executor moved to lib to be reused by plugins.
 */
package org.sonar.plugins.pmd;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PMDVersion;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.lang.rule.RuleSetLoadException;
import net.sourceforge.pmd.lang.rule.RuleSetLoader;
import net.sourceforge.pmd.reporting.FileAnalysisListener;
import net.sourceforge.pmd.reporting.Report;
import net.sourceforge.pmd.util.log.PmdReporter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleScope;
import org.sonar.plugins.pmd.xml.PmdRuleSet;
import org.sonar.plugins.pmd.xml.PmdRuleSets;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class AbstractPmdExecutor {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractPmdExecutor.class);

    protected final FileSystem fs;
    protected final ActiveRules rulesProfile;
    protected final PmdConfiguration pmdConfiguration;
    protected final Configuration settings;

    protected AbstractPmdExecutor(FileSystem fileSystem, ActiveRules rulesProfile,
                       PmdConfiguration pmdConfiguration, Configuration settings) {
        this.fs = fileSystem;
        this.rulesProfile = rulesProfile;
        this.pmdConfiguration = pmdConfiguration;
        this.settings = settings;
    }

    protected static void accept(FileAnalysisListener fal) {
        LOGGER.debug("Got FileAnalysisListener: {}", fal);
    }

    public Report execute() {
        final long startTimeMs = System.currentTimeMillis();
        LOGGER.info(getStartMessage(), PMDVersion.VERSION);
        final ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();

        try (URLClassLoader classLoader = createClassloader()) {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return executePmd(classLoader);
        } catch (IOException e) {
            LOGGER.error("Failed to close URLClassLoader.", e);
        } finally {
            Thread.currentThread().setContextClassLoader(initialClassLoader);
            LOGGER.info(getEndMessage(), PMDVersion.VERSION, System.currentTimeMillis() - startTimeMs);
        }

        return null;
    }

    protected abstract String getStartMessage();

    protected abstract String getEndMessage();

    protected Report executeLanguage(URLClassLoader classLoader, String languageKey, String repositoryKey) {
        final PmdTemplate pmdFactory = createPmdTemplate(classLoader);
        final Optional<Report> mainReport = executeRules(pmdFactory, hasFiles(Type.MAIN, languageKey), repositoryKey, RuleScope.MAIN);
        final Optional<Report> testReport = executeRules(pmdFactory, hasFiles(Type.TEST, languageKey), repositoryKey, RuleScope.TEST);

        if (LOGGER.isDebugEnabled()) {
            mainReport.ifPresent(this::writeDebugLine);
            testReport.ifPresent(this::writeDebugLine);
        }

        Consumer<FileAnalysisListener> fileAnalysisListenerConsumer = AbstractPmdExecutor::accept;

        Report unionReport = Report.buildReport(fileAnalysisListenerConsumer);
        unionReport = mainReport.map(unionReport::union).orElse(unionReport);
        unionReport = testReport.map(unionReport::union).orElse(unionReport);

        pmdConfiguration.dumpXmlReport(unionReport);

        return unionReport;
    }

    protected abstract URLClassLoader createClassloader();

    protected abstract Report executePmd(URLClassLoader classLoader);

    protected void writeDebugLine(Report r) {
        LOGGER.debug("Report (violations, suppressedViolations, processingErrors, configurationErrors): {}, {}, {}, {}", r.getViolations().size(), r.getSuppressedViolations().size(), r.getProcessingErrors().size(), r.getConfigurationErrors().size());
        if (!r.getViolations().isEmpty()) {
            LOGGER.debug("Violations: {}", r.getViolations());
        }
        if (!r.getSuppressedViolations().isEmpty()) {
            LOGGER.debug("SuppressedViolations: {}", r.getSuppressedViolations());
        }
        if (!r.getProcessingErrors().isEmpty()) {
            LOGGER.debug("ProcessingErrors: {}", r.getProcessingErrors());
        }
        if (!r.getConfigurationErrors().isEmpty()) {
            LOGGER.debug("ConfigurationErrors: {}", r.getConfigurationErrors());
        }
    }

    protected Iterable<InputFile> hasFiles(Type fileType, String languageKey) {
        final FilePredicates predicates = fs.predicates();
        return fs.inputFiles(
                predicates.and(
                        predicates.hasLanguage(languageKey),
                        predicates.hasType(fileType)
                )
        );
    }

    protected Optional<Report> executeRules(PmdTemplate pmdFactory, Iterable<InputFile> files, String repositoryKey, RuleScope scope) {
        if (!files.iterator().hasNext()) {
            LOGGER.debug("No files to analyze for {}", repositoryKey);
            return Optional.empty();
        }

        final RuleSet ruleSet = createRuleSet(repositoryKey, scope);

        if (ruleSet.size() < 1) {
            LOGGER.debug("No rules to apply for {}", repositoryKey);
            return Optional.empty();
        }

        LOGGER.debug("Found {} rules for {}", ruleSet.size(), repositoryKey);
        return Optional.ofNullable(pmdFactory.process(files, ruleSet));
    }

    protected RuleSet createRuleSet(String repositoryKey, RuleScope scope) {
        final String rulesXml = dumpXml(rulesProfile, repositoryKey, scope);
        final File ruleSetFile = pmdConfiguration.dumpXmlRuleSet(repositoryKey, rulesXml, scope);
        final String ruleSetFilePath = ruleSetFile.getAbsolutePath();

        try {
            PmdReporter reporter = createSonarPmdPluginLogger();
            return parseRuleSetWithReporter(reporter, ruleSetFilePath);
        } catch (RuleSetLoadException e) {
            throw new IllegalStateException(e);
        }
    }

    private static RuleSet parseRuleSetWithReporter(PmdReporter reporter, String ruleSetFilePath) {
        PMDConfiguration pmdConfiguration = new PMDConfiguration();
        pmdConfiguration.setReporter(reporter);
        RuleSetLoader loader = RuleSetLoader.fromPmdConfig(pmdConfiguration);
        return loader.loadFromResource(ruleSetFilePath);
    }

    private static @NotNull PmdReporter createSonarPmdPluginLogger() {
        PmdReporter reporter = new PmdReporter() {
            AtomicInteger numErrors = new AtomicInteger(0);
            @Override
            public boolean isLoggable(Level level) {
                return Level.ERROR.equals(level);
            }

            @Override
            public void logEx(Level level, @Nullable String message, Object[] formatArgs, @Nullable Throwable error) {
                numErrors.incrementAndGet();
                if (message == null) {
                    message = "<null>";
                }
                switch (level) {
                    case ERROR:
                        LOGGER.error(String.format(message, formatArgs));
                        break;
                    case WARN:
                        LOGGER.debug(String.format(message, formatArgs));
                        break;
                    case INFO:
                        LOGGER.debug(String.format(message, formatArgs));
                        break;
                    case DEBUG:
                        LOGGER.debug(String.format(message, formatArgs));
                        break;
                    case TRACE:
                        LOGGER.trace(String.format(message, formatArgs));
                        break;
                    default:
                        LOGGER.warn("Unknown PMD log level: {} message: {}", level, String.format(message, formatArgs));
                }
            }

            @Override
            public int numErrors() {
                return numErrors.get();
            }
        };
        return reporter;
    }

    protected String dumpXml(ActiveRules rulesProfile, String repositoryKey, RuleScope scope) {
        final StringWriter writer = new StringWriter(2048);
        final PmdRuleSet ruleSet = PmdRuleSets.from(rulesProfile, repositoryKey, scope);
        ruleSet.writeTo(writer);

        return writer.toString();
    }

    protected PmdTemplate createPmdTemplate(URLClassLoader classLoader) {
        return PmdTemplate.create(getSourceVersion(), classLoader, fs.encoding());
    }

    protected String getSourceVersion() {
        String reqJavaVersion = settings.get(PmdConstants.JAVA_SOURCE_VERSION).orElse(PmdConstants.JAVA_SOURCE_VERSION_DEFAULT_VALUE);
        String bareReqJavaVersion = reqJavaVersion;
        if (reqJavaVersion.endsWith("-preview")) {
            bareReqJavaVersion = reqJavaVersion.substring(0, reqJavaVersion.indexOf("-preview"));
        }
        String effectiveJavaVersion = bareReqJavaVersion;
        if (Float.parseFloat(bareReqJavaVersion) >= Float.parseFloat(PmdConstants.JAVA_SOURCE_MINIMUM_UNSUPPORTED_VALUE)) {
            effectiveJavaVersion = PmdConstants.JAVA_SOURCE_MAXIMUM_SUPPORTED_VALUE;
            LOGGER.warn("Requested Java version {} ('{}') is not supported by PMD. Using maximum supported version: {}.",
                    reqJavaVersion, PmdConstants.JAVA_SOURCE_VERSION, PmdConstants.JAVA_SOURCE_MAXIMUM_SUPPORTED_VALUE);
        }
        return effectiveJavaVersion;
    }
}

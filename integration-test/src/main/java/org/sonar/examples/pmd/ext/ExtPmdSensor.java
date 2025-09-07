package org.sonar.examples.pmd.ext;

import net.sourceforge.pmd.reporting.RuleViolation;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

public class ExtPmdSensor implements Sensor {
    private final ActiveRules activeRules;
    private final ExtJavaExecutor javaExecutor;
    private final ExtViolationRecorder violationRecorder;
    private final FileSystem fs;

    public ExtPmdSensor(ActiveRules activeRules,
                        ExtJavaExecutor javaExecutor,
                        ExtViolationRecorder violationRecorder,
                        FileSystem fs) {
        this.activeRules = activeRules;
        this.javaExecutor = javaExecutor;
        this.violationRecorder = violationRecorder;
        this.fs = fs;
    }

    private boolean hasFilesToCheck(Type type) {
        FilePredicates predicates = fs.predicates();
        return fs.hasFiles(predicates.and(
                predicates.hasLanguage(ExtConstants.LANGUAGE_JAVA_KEY),
                predicates.hasType(type))) && !activeRules.findByRepository(ExtConstants.REPOSITORY_KEY).isEmpty();
    }

    private boolean shouldExecuteOnProject() {
        return hasFilesToCheck(Type.MAIN) || hasFilesToCheck(Type.TEST);
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(ExtConstants.LANGUAGE_JAVA_KEY)
                .name("ExtPmdSensor");
    }

    @Override
    public void execute(SensorContext context) {
        if (!shouldExecuteOnProject()) {
            return;
        }
        var report = javaExecutor.execute();
        if (report != null) {
            for (RuleViolation violation : report.getViolations()) {
                violationRecorder.saveViolation(violation, context);
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

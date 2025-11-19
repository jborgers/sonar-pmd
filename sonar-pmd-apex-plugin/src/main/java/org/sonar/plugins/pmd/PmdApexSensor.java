/*
 * SonarQube PMD7 Plugin - Apex module
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

import net.sourceforge.pmd.reporting.RuleViolation;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

/**
 * Sensor for running PMD on Apex files only.
 */
public class PmdApexSensor implements Sensor {
    private final ActiveRules profile;
    private final PmdApexExecutor apexExecutor;
    private final PmdViolationRecorder pmdViolationRecorder;
    private final FileSystem fs;

    public PmdApexSensor(ActiveRules profile, PmdApexExecutor apexExecutor, PmdViolationRecorder pmdViolationRecorder, FileSystem fs) {
        this.profile = profile;
        this.apexExecutor = apexExecutor;
        this.pmdViolationRecorder = pmdViolationRecorder;
        this.fs = fs;
    }

    private boolean shouldExecuteOnProject() {
        return hasFilesToCheck(Type.MAIN, PmdConstants.MAIN_APEX_REPOSITORY_KEY, PmdConstants.LANGUAGE_APEX_KEY)
            || hasFilesToCheck(Type.TEST, PmdConstants.MAIN_APEX_REPOSITORY_KEY, PmdConstants.LANGUAGE_APEX_KEY);
    }

    private boolean hasFilesToCheck(Type type, String repositoryKey, String languageKey) {
        FilePredicates predicates = fs.predicates();
        final boolean hasMatchingFiles = fs.hasFiles(predicates.and(
            predicates.hasLanguage(languageKey),
            predicates.hasType(type)));
        return hasMatchingFiles && !profile.findByRepository(repositoryKey).isEmpty();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguages(PmdConstants.LANGUAGE_APEX_KEY)
            .name("PmdApexSensor");
    }

    @Override
    public void execute(SensorContext context) {
        if (!shouldExecuteOnProject()) {
            return;
        }
        net.sourceforge.pmd.reporting.Report apexReport = apexExecutor.execute();
        if (apexReport != null) {
            for (RuleViolation violation : apexReport.getViolations()) {
                pmdViolationRecorder.saveViolation(violation, context);
            }
        }
    }
}

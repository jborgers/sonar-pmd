/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.pmd;

import com.google.common.collect.Iterables;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleViolation;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.XmlParserException;
import org.sonar.plugins.java.Java;

import java.io.File;

public class PmdSensor implements Sensor {
  private final RulesProfile profile;
  private final PmdExecutor executor;
  private final PmdViolationRecorder pmdViolationRecorder;
  private final FileSystem fs;

  public PmdSensor(RulesProfile profile, PmdExecutor executor, PmdViolationRecorder pmdViolationRecorder, FileSystem fs) {
    this.profile = profile;
    this.executor = executor;
    this.pmdViolationRecorder = pmdViolationRecorder;
    this.fs = fs;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return (hasFilesToCheck(Type.MAIN, PmdConstants.REPOSITORY_KEY))
      || (hasFilesToCheck(Type.TEST, PmdConstants.TEST_REPOSITORY_KEY));
  }

  private boolean hasFilesToCheck(Type type, String repositoryKey) {
    FilePredicates predicates = fs.predicates();
    Iterable<File> files = fs.files(predicates.and(
      predicates.hasLanguage(Java.KEY),
      predicates.hasType(type)));
    return !Iterables.isEmpty(files) && !profile.getActiveRulesByRepository(repositoryKey).isEmpty();
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    try {
      Report report = executor.execute();
      for (RuleViolation violation : report) {
        pmdViolationRecorder.saveViolation(violation);
      }
    } catch (Exception e) {
      throw new XmlParserException(e);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

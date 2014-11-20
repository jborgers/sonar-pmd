/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.TimeProfiler;
import org.sonar.java.api.JavaUtils;
import org.sonar.plugins.java.Java;
import org.sonar.plugins.java.api.JavaResourceLocator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

public class PmdExecutor implements BatchExtension {
  private final Project project;
  private final FileSystem fs;
  private final RulesProfile rulesProfile;
  private final PmdProfileExporter pmdProfileExporter;
  private final PmdConfiguration pmdConfiguration;
  private final JavaResourceLocator javaResourceLocator;

  public PmdExecutor(Project project, FileSystem fileSystem, RulesProfile rulesProfile,
    PmdProfileExporter pmdProfileExporter, PmdConfiguration pmdConfiguration, JavaResourceLocator javaResourceLocator) {
    this.project = project;
    this.fs = fileSystem;
    this.rulesProfile = rulesProfile;
    this.pmdProfileExporter = pmdProfileExporter;
    this.pmdConfiguration = pmdConfiguration;
    this.javaResourceLocator = javaResourceLocator;
  }

  public Report execute() {
    TimeProfiler profiler = new TimeProfiler().start("Execute PMD " + PmdVersion.getVersion());

    ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

      return executePmd();
    } finally {
      Thread.currentThread().setContextClassLoader(initialClassLoader);
      profiler.stop();
    }
  }

  private Report executePmd() {
    Report report = new Report();

    RuleContext context = new RuleContext();
    context.setReport(report);

    PmdTemplate pmdFactory = createPmdTemplate();
    executeRules(pmdFactory, context, javaFiles(Type.MAIN), PmdConstants.REPOSITORY_KEY);
    executeRules(pmdFactory, context, javaFiles(Type.TEST), PmdConstants.TEST_REPOSITORY_KEY);

    pmdConfiguration.dumpXmlReport(report);

    return report;
  }

  public Iterable<File> javaFiles(Type fileType) {
    FilePredicates predicates = fs.predicates();
    return fs.files(predicates.and(
      predicates.hasLanguage(Java.KEY),
      predicates.hasType(fileType)));
  }

  public void executeRules(PmdTemplate pmdFactory, RuleContext ruleContext, Iterable<File> files, String repositoryKey) {
    if (Iterables.isEmpty(files)) {
      // Nothing to analyze
      return;
    }

    RuleSets rulesets = createRulesets(repositoryKey);
    if (rulesets.getAllRules().isEmpty()) {
      // No rule
      return;
    }

    rulesets.start(ruleContext);

    for (File file : files) {
      pmdFactory.process(file, rulesets, ruleContext);
    }

    rulesets.end(ruleContext);
  }

  private RuleSets createRulesets(String repositoryKey) {
    String rulesXml = pmdProfileExporter.exportProfile(repositoryKey, rulesProfile);
    File ruleSetFile = pmdConfiguration.dumpXmlRuleSet(repositoryKey, rulesXml);
    String ruleSetFilePath = ruleSetFile.getAbsolutePath();
    RuleSetFactory ruleSetFactory = new RuleSetFactory();
    try {
      RuleSet ruleSet = ruleSetFactory.createRuleSet(ruleSetFilePath);
      return new RuleSets(ruleSet);
    } catch (RuleSetNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  @VisibleForTesting
  PmdTemplate createPmdTemplate() {
    ClassLoader projectClassLoader = createClassloader();
    Charset encoding = fs.encoding();
    return PmdTemplate.create(JavaUtils.getSourceVersion(project), projectClassLoader, encoding);
  }

  private ClassLoader createClassloader() {
    Collection<File> classpathElements = javaResourceLocator.classpath();
    List<URL> urls = Lists.newArrayList();
    for (File file : classpathElements) {
      try {
        urls.add(file.toURI().toURL());
      } catch (MalformedURLException e) {
        throw new IllegalStateException("Fail to create the project classloader. Classpath element is invalid: " + file, e);
      }
    }
    return new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
  }

}

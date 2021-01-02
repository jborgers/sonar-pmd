/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.plugins.pmd.integration;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.fs.internal.Metadata;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.java.DefaultJavaResourceLocator;
import org.sonar.java.JavaClasspath;
import org.sonar.plugins.java.api.JavaResourceLocator;
import org.sonar.plugins.pmd.*;
import org.sonar.plugins.pmd.rule.PmdRulesDefinition;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

class PmdIT {

    @Test
    void testo() throws URISyntaxException, IOException {

        final Path p = Paths.get(PmdIT.class.getResource("/integration/AvoidDuplicateLiterals.java").toURI());

        final SensorContextTester ctx = SensorContextTester.create(p.getParent());


        final RulesDefinition.Context ruleContext = new RulesDefinition.Context();
        final PmdRulesDefinition pmdRulesDefinition = new PmdRulesDefinition();
        pmdRulesDefinition.define(ruleContext);

        RulesDefinition.Rule rule = ruleContext.repository("pmd")
                .rule("AvoidDuplicateLiterals");

        RuleKey of = RuleKey.of("pmd", "AvoidDuplicateLiterals");

        NewActiveRule rule1 = new NewActiveRule.Builder()
                .setRuleKey(of)
                .setInternalKey(rule.internalKey())
                .setName(rule.name())
                .setSeverity(rule.severity())
                .setLanguage(PmdConstants.LANGUAGE_KEY)
                .setParam("maxDuplicateLiterals", "4")
                .setParam("skipAnnotations", "false")
                .setParam("minimumLength", "3")
                .build();

        final ActiveRules rules = new ActiveRulesBuilder()
                .addRule(rule1)
                .build();
        ctx.setActiveRules(rules);

        final DefaultFileSystem fileSystem = ctx.fileSystem();
        final Configuration config = ctx.config();

        Metadata metadata = null;

        try (Reader r = Files.newBufferedReader(p)) {
            metadata = new FileMetadata().readMetadata(r);
        }

        final InputFile file = TestInputFileBuilder.create("integration-test", p.getParent().toFile(), p.toFile())
                .setLanguage(PmdConstants.LANGUAGE_KEY)
                .setType(InputFile.Type.MAIN)
                .setMetadata(metadata)
                .build();

        fileSystem.setWorkDir(p.getParent());
        fileSystem.add(file);

        final JavaClasspath classpath = new JavaClasspath(config, fileSystem);
        final JavaResourceLocator javaResourceLocator = new DefaultJavaResourceLocator(classpath);

        final PmdConfiguration configuration = new PmdConfiguration(fileSystem, config);
        final PmdExecutor executor = new PmdExecutor(fileSystem, rules, configuration, javaResourceLocator, config);
        final PmdViolationRecorder recorder = new PmdViolationRecorder(fileSystem, rules);

        new PmdSensor(rules, executor, recorder, fileSystem)
                .execute(ctx);
        Collection<Issue> issues = ctx.allIssues();
    }
}

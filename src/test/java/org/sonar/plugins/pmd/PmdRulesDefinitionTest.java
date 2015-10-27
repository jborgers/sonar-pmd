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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.sonar.api.PropertyType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Param;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.plugins.java.Java;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class PmdRulesDefinitionTest {

  @Test
  public void test() {
    PmdRulesDefinition definition = new PmdRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
    RulesDefinition.Repository repository = context.repository(PmdConstants.REPOSITORY_KEY);

    assertThat(repository.name()).isEqualTo(PmdConstants.REPOSITORY_NAME);
    assertThat(repository.language()).isEqualTo(Java.KEY);

    List<Rule> rules = repository.rules();
    assertThat(rules).hasSize(270);

    for (Rule rule : rules) {
      assertThat(rule.key()).isNotNull();
      assertThat(rule.internalKey()).isNotNull();
      assertThat(rule.name()).isNotNull();
      assertThat(rule.htmlDescription()).isNotNull();
      assertThat(rule.severity()).isNotNull();

      for (Param param : rule.params()) {
        assertThat(param.name()).isNotNull();
        assertThat(param.description())
          .overridingErrorMessage("Description is not set for parameter '" + param.name() + "' of rule '" + rule.key())
          .isNotNull();
      }

      if (!"XPathRule".equals(rule.key())) {
        assertThat(rule.debtRemediationFunction())
          .overridingErrorMessage("Sqale remediation function is not set for rule '" + rule.key())
          .isNotNull();
        assertThat(rule.debtSubCharacteristic())
          .overridingErrorMessage("Sqale characteristic is not set for rule '" + rule.key())
          .isNotNull();
      }
    }
  }

  @Test
  public void should_exclude_junit_rules() {
    PmdRulesDefinition definition = new PmdRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
    RulesDefinition.Repository repository = context.repository(PmdConstants.REPOSITORY_KEY);

    for (Rule rule : repository.rules()) {
      assertThat(rule.key()).excludes("JUnitStaticSuite");
    }
  }

  @Test
  public void should_use_text_parameter_for_xpath_rule() {
    PmdRulesDefinition definition = new PmdRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
    RulesDefinition.Repository repository = context.repository(PmdConstants.REPOSITORY_KEY);

    Rule xpathRule = Iterables.find(repository.rules(), new Predicate<Rule>() {
      @Override
      public boolean apply(Rule rule) {
        return rule.key().equals("XPathRule");
      }
    });

    assertThat(xpathRule.param("xpath").type().type()).isEqualTo(PropertyType.TEXT.name());
  }
}

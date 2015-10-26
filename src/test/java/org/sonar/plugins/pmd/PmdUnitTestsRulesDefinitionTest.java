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

import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Param;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.plugins.java.Java;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class PmdUnitTestsRulesDefinitionTest {

  @Test
  public void test() {

    PmdUnitTestsRulesDefinition definition = new PmdUnitTestsRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
    RulesDefinition.Repository repository = context.repository(PmdConstants.TEST_REPOSITORY_KEY);

    assertThat(repository.name()).isEqualTo(PmdConstants.TEST_REPOSITORY_NAME);
    assertThat(repository.language()).isEqualTo(Java.KEY);

    List<Rule> rules = repository.rules();
    assertThat(rules).hasSize(17);

    for (Rule rule : rules) {
      assertThat(rule.key()).isNotNull();
      assertThat(rule.key()).isIn(
        "JUnitStaticSuite",
        "JUnitSpelling",
        "JUnitAssertionsShouldIncludeMessage",
        "JUnitTestsShouldIncludeAssert",
        "TestClassWithoutTestCases",
        "UnnecessaryBooleanAssertion",
        "UseAssertEqualsInsteadOfAssertTrue",
        "UseAssertSameInsteadOfAssertTrue",
        "UseAssertNullInsteadOfAssertTrue",
        "SimplifyBooleanAssertion",
        "UseAssertTrueInsteadOfAssertEquals",
        "JUnitTestContainsTooManyAsserts",
        "JUnit4SuitesShouldUseSuiteAnnotation",
        "JUnit4TestShouldUseAfterAnnotation",
        "JUnit4TestShouldUseBeforeAnnotation",
        "JUnit4TestShouldUseTestAnnotation",
        "JUnitUseExpected");
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
}

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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.XMLRuleParser;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class PmdUnitTestsRuleRepositoryTest {
  PmdUnitTestsRuleRepository repository;

  @Before
  public void setUpRuleRepository() {
    repository = new PmdUnitTestsRuleRepository(new XMLRuleParser());
  }

  @Test
  public void should_have_correct_name_and_key() {
    assertThat(repository.getKey()).isEqualTo("pmd-unit-tests");
    assertThat(repository.getLanguage()).isEqualTo("java");
    assertThat(repository.getName()).isEqualTo("PMD Unit Tests");
  }

  @Test
  public void should_load_repository_from_xml() {
    List<Rule> rules = repository.createRules();

    assertThat(rules).onProperty("key").containsOnly(
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
  }

  @Test
  public void should_provide_a_name_and_description_for_each_rule() throws Exception {
    PmdRuleRepositoryTest.assertThatAllRulesHaveNonEmptyNameAndDescription(repository);
  }
}

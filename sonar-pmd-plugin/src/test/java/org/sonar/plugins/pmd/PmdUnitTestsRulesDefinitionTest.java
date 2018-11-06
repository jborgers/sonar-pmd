/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2018 SonarSource SA
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
package org.sonar.plugins.pmd;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Param;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.plugins.pmd.rule.PmdUnitTestsRulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class PmdUnitTestsRulesDefinitionTest {

    @Test
    void test() {

        PmdUnitTestsRulesDefinition definition = new PmdUnitTestsRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();
        definition.define(context);
        RulesDefinition.Repository repository = context.repository(PmdConstants.TEST_REPOSITORY_KEY);

        assertThat(repository)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", PmdConstants.TEST_REPOSITORY_NAME)
                .hasFieldOrPropertyWithValue("language", PmdConstants.LANGUAGE_KEY);

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
        }
    }
}

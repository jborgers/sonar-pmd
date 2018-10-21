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

package org.sonar.plugins.pmd.xml;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.utils.ValidationMessages;

import static org.assertj.core.api.Assertions.assertThat;

class PmdRuleSetsTest {

    private ValidationMessages messages;

    @BeforeEach
    void setup() {
        messages = ValidationMessages.create();
    }

    @Test
    void whenValidXmlGivenThenPmdRuleSetIsReturned() throws URISyntaxException, IOException {

        // given
        final Reader reader = createReader("/org/sonar/plugins/pmd/simple.xml");

        // when
        final PmdRuleSet result = PmdRuleSets.parse(reader, messages);

        // then
        assertThat(result).isNotNull()
                .satisfies(this::hasRuleCouplingBetweenObjects)
                .satisfies(this::hasRuleExcessiveImports)
                .satisfies(this::hasRuleUseNotifyAllInsteadOfNotify)
                .satisfies(this::hasRuleUseCollectionIsEmptyRule);

        assertThat(result.getPmdRules()).hasSize(4);
        assertThatNoMessagesWritten();
    }

    @Test
    void whenExceptionOccursWhileReadingThenEmptyRuleSetIsReturned() {

        // given
        final Reader nullReader = null;

        // when
        final PmdRuleSet result = PmdRuleSets.parse(nullReader, messages);

        // then
        assertThat(result)
                .isNotNull()
                .extracting("rules")
                .element(0)
                .asList()
                .isEmpty();

        assertThat(messages.getErrors())
                .isNotEmpty();
    }

    private void assertThatNoMessagesWritten() {
        assertThat(messages.getInfos()).isEmpty();
        assertThat(messages.getWarnings()).isEmpty();
        assertThat(messages.getErrors()).isEmpty();
    }

    private void hasRuleCouplingBetweenObjects(PmdRuleSet pmdRuleSet) {
        final Optional<PmdRule> couplingBetweenObjects = pmdRuleSet.getPmdRules()
                .stream()
                .filter(rule -> rule.getRef() != null)
                .filter(rule -> rule.getRef().endsWith("CouplingBetweenObjects"))
                .findFirst();

        assertThat(couplingBetweenObjects).isPresent()
                .get()
                .hasFieldOrPropertyWithValue("priority", "2")
                .extracting("properties")
                .element(0)
                .asList()
                .element(0)
                .hasFieldOrPropertyWithValue("name", "threshold")
                .hasFieldOrPropertyWithValue("value", "20")
                .hasNoNullFieldsOrPropertiesExcept("cdataValue");
    }

    private void hasRuleUseNotifyAllInsteadOfNotify(PmdRuleSet pmdRuleSet) {
        final Optional<PmdRule> useNotifyAllInsteadOfNotify = pmdRuleSet.getPmdRules()
                .stream()
                .filter(rule -> rule.getRef() != null)
                .filter(rule -> rule.getRef().endsWith("UseNotifyAllInsteadOfNotify"))
                .findFirst();

        assertThat(useNotifyAllInsteadOfNotify).isPresent()
                .get()
                .hasFieldOrPropertyWithValue("priority", "4")
                .extracting("properties")
                .element(0)
                .asList()
                .isEmpty();
    }

    private void hasRuleExcessiveImports(PmdRuleSet pmdRuleSet) {
        final Optional<PmdRule> excessiveImports = pmdRuleSet.getPmdRules()
                .stream()
                .filter(rule -> rule.getRef() != null)
                .filter(rule -> rule.getRef().endsWith("ExcessiveImports"))
                .findFirst();

        assertThat(excessiveImports).isPresent()
                .get()
                .hasFieldOrPropertyWithValue("priority", null)
                .extracting("properties")
                .element(0)
                .asList()
                .element(0)
                .hasFieldOrPropertyWithValue("name", "minimum")
                .hasFieldOrPropertyWithValue("value", "30");
    }

    private void hasRuleUseCollectionIsEmptyRule(PmdRuleSet pmdRuleSet) {
        final Optional<PmdRule> couplingBetweenObjects = pmdRuleSet.getPmdRules()
                .stream()
                .filter(rule -> rule.getClazz() != null)
                .filter(rule -> rule.getClazz().endsWith("UseCollectionIsEmptyRule"))
                .findFirst();

        assertThat(couplingBetweenObjects).isPresent()
                .get()
                .hasFieldOrPropertyWithValue("priority", "3")
                .extracting("properties")
                .element(0).asList()
                .isEmpty();
    }

    private Reader createReader(String path) throws URISyntaxException, IOException {
        final URI resource = PmdRuleSetsTest.class.getResource(path).toURI();
        return Files.newBufferedReader(
                Paths.get(resource),
                StandardCharsets.UTF_8
        );
    }
}
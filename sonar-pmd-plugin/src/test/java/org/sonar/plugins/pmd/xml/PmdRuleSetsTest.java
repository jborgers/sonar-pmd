/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2021 SonarSource SA and others
 * mailto:jens AT gerdes DOT digital
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PmdRuleSetsTest {

    private ValidationMessages messages;

    @BeforeEach
    void setup() {
        messages = ValidationMessages.create();
    }

    @Test
    void whenClosingTheResourceAfterParsingFailsThenReturnsResultQuietly() {

        // given
        final Reader mockReader = mock(Reader.class, invocationOnMock -> {
            throw new IOException();
        });

        // when
        final PmdRuleSet result = PmdRuleSets.from(mockReader, messages);

        // then
        assertThat(result)
                .isNotNull()
                .extracting("rules")
                .asList()
                .isEmpty();
    }

    @Test
    void whenActiveRulesGivenThenRuleSetIsReturned() {

        // given
        final ActiveRules mockedRules = mock(ActiveRules.class);
        final String anyRepoKey = "TEST";

        // when
        final PmdRuleSet result = PmdRuleSets.from(mockedRules, anyRepoKey);

        // then
        assertThat(result)
                .isNotNull();
    }

    @Test
    void whenRulesProfileGivenThenRuleSetIsReturned() {

        // given
        final RulesProfile mockedProfile = mock(RulesProfile.class);
        final String anyRepoKey = "TEST";

        // when
        final PmdRuleSet result = PmdRuleSets.from(mockedProfile, anyRepoKey);

        // then
        assertThat(result)
                .isNotNull();
    }
}
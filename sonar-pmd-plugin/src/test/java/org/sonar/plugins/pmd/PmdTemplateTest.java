/*
 * SonarQube PMD7 Plugin
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

import net.sourceforge.pmd.lang.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

class PmdTemplateTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "6", "7", "8", "9", "1.9", "10", "1.10", "11", "1.11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"
    })
    void verifyCanHandleJavaLanguageVersion(String javaVersion) {
        final Language language = PmdTemplate
                .languageVersion(javaVersion)
                .getLanguage();

        assertThat(language).isNotNull();
    }

    @Test
    void should_fail_on_invalid_java_version() {

        // when
        final Throwable thrown = catchThrowable(() -> PmdTemplate.create("12.2", mock(ClassLoader.class), StandardCharsets.UTF_8));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldnt_fail_on_valid_java_version() {

        // when
        PmdTemplate result = PmdTemplate.create("6", mock(ClassLoader.class), StandardCharsets.UTF_8);

        // then
        assertThat(result)
                .isNotNull();
    }

    /**
     * SONARPLUGINS-3318
     */
    @Test
    void should_set_classloader() {
        ClassLoader classloader = mock(ClassLoader.class);
        PmdTemplate pmdTemplate = PmdTemplate.create("6", classloader, StandardCharsets.UTF_8);
        assertThat(pmdTemplate.configuration().getClassLoader()).isEqualTo(classloader);
    }

    @Test
    void should_set_encoding() {
        PmdTemplate pmdTemplate = PmdTemplate.create("6", mock(ClassLoader.class), StandardCharsets.UTF_16BE);
        assertThat(pmdTemplate.configuration().getSourceEncoding()).isEqualTo(StandardCharsets.UTF_16BE);
    }

}

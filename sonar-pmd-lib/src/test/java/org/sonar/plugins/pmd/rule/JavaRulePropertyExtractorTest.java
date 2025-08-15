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
package org.sonar.plugins.pmd.rule;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.pmd.rule.JavaRulePropertyExtractor.PropertyInfo;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class JavaRulePropertyExtractorTest {

    @Test
    void shouldExtractPropertiesFromJar() throws IOException {
        // given
        JavaRulePropertyExtractor extractor = new JavaRulePropertyExtractor();
        String jarPath = getTestJarPath();
        
        // Skip test if jar file doesn't exist (this is just a placeholder test)
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            return;
        }

        // when
        Map<String, List<PropertyInfo>> properties = extractor.extractProperties(jarPath);

        // then
        assertThat(properties.size()).isGreaterThan(100);
        // Assert that all lists are non-null
        assertThat(properties.values()).allMatch(Objects::nonNull);
        // This core rule should not blow things up
        assertThat(properties.get("net.sourceforge.pmd.lang.rule.impl.UnnecessaryPmdSuppressionRule")).isNotNull();

        List<PropertyInfo> propertyInfos = properties.get("net.sourceforge.pmd.lang.java.rule.errorprone.AvoidDuplicateLiteralsRule");
        assertThat(propertyInfos.size()).isEqualTo(6);
        assertThat(propertyInfos.stream().filter(p -> p.getName().equals("maxDuplicateLiterals")).findFirst().get().getType()).isEqualTo("Integer");
        assertThat(propertyInfos.stream().filter(p -> p.getName().equals("skipAnnotations")).findFirst().get().getType()).isEqualTo("Boolean");
        assertThat(propertyInfos.stream().filter(p -> p.getName().equals("exceptionList")).findFirst().get().getType()).isEqualTo("Set");
    }

    @Test
    void shouldHaveEmptyDefaultValues() {
        // given
        List<String> defaultValues = Collections.emptyList();

        // when
        PropertyInfo propertyInfo = new PropertyInfo("testName", "Test Description", "STRING", defaultValues);

        // then
        assertThat(propertyInfo.getName()).isEqualTo("testName");
        assertThat(propertyInfo.getDescription()).isEqualTo("Test Description");
        assertThat(propertyInfo.getType()).isEqualTo("STRING");
        assertThat(propertyInfo.getDefaultValues()).hasSize(0);
        assertThat(propertyInfo.getDefaultValuesAsString()).isEqualTo("");
    }

    @Test
    void propertyInfoShouldHandleDefaultValues() {
        // given
        List<String> defaultValues = Arrays.asList("value1", "value2", "value3");
        
        // when
        PropertyInfo propertyInfo = new PropertyInfo("testName", "Test Description", "STRING", defaultValues);
        
        // then
        assertThat(propertyInfo.getName()).isEqualTo("testName");
        assertThat(propertyInfo.getDescription()).isEqualTo("Test Description");
        assertThat(propertyInfo.getType()).isEqualTo("STRING");
        assertThat(propertyInfo.getDefaultValues()).containsExactly("value1", "value2", "value3");
        assertThat(propertyInfo.getDefaultValuesAsString()).isEqualTo("value1,value2,value3");
    }

    @Test
    void propertyInfoShouldHandleEmptyDefaultValues() {
        // given
        List<String> emptyList = List.of();
        
        // when
        PropertyInfo propertyInfo = new PropertyInfo("testName", "Test Description", "BOOLEAN", emptyList);
        
        // then
        assertThat(propertyInfo.getName()).isEqualTo("testName");
        assertThat(propertyInfo.getDescription()).isEqualTo("Test Description");
        assertThat(propertyInfo.getType()).isEqualTo("BOOLEAN");
        assertThat(propertyInfo.getDefaultValues()).isEmpty();
        assertThat(propertyInfo.getDefaultValuesAsString()).isEmpty();
    }

    private String getTestJarPath() {
        // This would be the path to a test jar file
        // In a real test, you might want to create a small test jar with known content
        return System.getProperty("user.home") + "/.m2/repository/net/sourceforge/pmd/pmd-java/7.15.0/pmd-java-7.15.0.jar";
    }
}
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
import org.sonar.plugins.pmd.rule.util.ZipBombProtection;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JavaRulePropertyExtractorTest {

    @Test
    void shouldThrowOnHighlyCompressedJar() throws IOException {
        // given
        JavaRulePropertyExtractor extractor = new JavaRulePropertyExtractor();
        File jarFile = getHighlyCompressedJarPath();
        if (!jarFile.exists()) {
            throw new IOException("Jar file " + jarFile + " does not exist.");
        }
        assertThatThrownBy(() -> extractor.extractProperties(jarFile))
                .isInstanceOf(ZipBombProtection.PossibleZipBombException.class);
    }

    @Test
    void shouldExtractPropertiesFromTestJar() throws IOException {
        // given
        JavaRulePropertyExtractor extractor = new JavaRulePropertyExtractor();
        File jarFile = getTestJarPath();

        if (!jarFile.exists()) {
            throw new IOException("Jar file " + jarFile + " does not exist.");
        }

        // when
        Map<String, List<PropertyInfo>> properties = extractor.extractProperties(jarFile);

        // then
        assertThat(properties.size()).isEqualTo(2);

        List<PropertyInfo> propertyInfos = properties.get("com.example.rules.WithPropsRule");
        assertThat(propertyInfos.size()).isEqualTo(6);
        assertThat(propertyInfos.stream().filter(p -> p.getName().equals("strProp")).findFirst().get().getType()).isEqualTo("String");
        assertThat(propertyInfos.stream().filter(p -> p.getName().equals("intProp")).findFirst().get().getType()).isEqualTo("Integer");
        assertThat(propertyInfos.stream().filter(p -> p.getName().equals("boolProp")).findFirst().get().getType()).isEqualTo("Boolean");
        assertThat(propertyInfos.stream().filter(p -> p.getName().equals("listStrProp")).findFirst().get().getType()).isEqualTo("ArrayList");

        propertyInfos = properties.get("com.example.rules.WithoutPropsRule");
        // there are always 2 default properties, equal to type Optional
        assertThat(propertyInfos.size()).isEqualTo(2);
        assertThat(propertyInfos.stream().filter(p -> p.getName().equals("violationSuppressRegex")).findFirst().get().getType()).isEqualTo("Optional");
        assertThat(propertyInfos.stream().filter(p -> p.getName().equals("violationSuppressXPath")).findFirst().get().getType()).isEqualTo("Optional");
    }

    @Test
    void shouldExtractPropertiesFromRealJar() throws IOException {
        // given
        JavaRulePropertyExtractor extractor = new JavaRulePropertyExtractor();
        File jarFile = getRealJarPath();

        if (!jarFile.exists()) {
            throw new IOException("Jar file " + jarFile + " does not exist.");
        }

        // when
        Map<String, List<PropertyInfo>> properties = extractor.extractProperties(jarFile);

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

    private File getTestJarPath() {
        return new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test-java-rule-extractor.jar")).getPath());
    }

    private File getHighlyCompressedJarPath() {
        return new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test-highly-compressed.jar")).getPath());
    }

    private File getRealJarPath() {
        return new File(System.getProperty("user.home") + "/.m2/repository/net/sourceforge/pmd/pmd-java/7.17.0/pmd-java-7.17.0.jar");
    }
}
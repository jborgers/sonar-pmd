// copy of https://github.com/SonarSource/sonar-plugin-api/blob/993f9bc9c6c7b671e0a8a14fdf4fb88c7b91e1ae/plugin-api/src/test/java/org/sonar/api/server/rule/RulesDefinitionXmlLoaderTest.java
// Don't remove copyright below!

/*
 * Sonar Plugin API
 * Copyright (C) 2009-2025 SonarSource SA
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

package org.sonar.plugins.pmd.rule;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.impl.RulesDefinitionContext;
import org.sonar.api.server.rule.RulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RulesDefinitionXmlLoaderTest {

    public static final Charset ENCODING = StandardCharsets.UTF_8;
    public static final String ENCODING_NAME = ENCODING.name();
    RulesDefinitionXmlLoader underTest = new RulesDefinitionXmlLoader();

    @Test
    public void parse_xml() {
        InputStream input = getClass().getResourceAsStream("RulesDefinitionXmlLoaderTest/rules.xml");
        RulesDefinition.Repository repository = load(input, ENCODING_NAME);
        assertThat(repository.rules()).hasSize(2);

        RulesDefinition.Rule rule = repository.rule("complete");
        assertThat(rule.key()).isEqualTo("complete");
        assertThat(rule.name()).isEqualTo("Complete");
        assertThat(rule.htmlDescription()).isEqualTo("Description of Complete");
        assertThat(rule.severity()).isEqualTo(Severity.BLOCKER);
        assertThat(rule.template()).isTrue();
        assertThat(rule.status()).isEqualTo(RuleStatus.BETA);
        assertThat(rule.internalKey()).isEqualTo("Checker/TreeWalker/LocalVariableName");
        assertThat(rule.type()).isEqualTo(RuleType.BUG);
        assertThat(rule.tags()).containsOnly("misra", "spring");

        assertThat(rule.params()).hasSize(2);
        RulesDefinition.Param ignore = rule.param("ignore");
        assertThat(ignore.key()).isEqualTo("ignore");
        assertThat(ignore.description()).isEqualTo("Ignore ?");
        assertThat(ignore.defaultValue()).isEqualTo("false");

        rule = repository.rule("minimal");
        assertThat(rule.key()).isEqualTo("minimal");
        assertThat(rule.name()).isEqualTo("Minimal");
        assertThat(rule.htmlDescription()).isEqualTo("Description of Minimal");
        assertThat(rule.params()).isEmpty();
        assertThat(rule.status()).isEqualTo(RuleStatus.READY);
        assertThat(rule.severity()).isEqualTo(Severity.MAJOR);
        assertThat(rule.type()).isEqualTo(RuleType.CODE_SMELL);
    }

    @Test
    public void fail_if_missing_rule_key() {
        assertThatThrownBy(() -> load(IOUtils.toInputStream("<rules><rule><name>Foo</name></rule></rules>", ENCODING), ENCODING_NAME))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void fail_if_missing_property_key() {
        assertThatThrownBy(() -> load(IOUtils.toInputStream("<rules><rule><key>foo</key><name>Foo</name><param></param></rule></rules>", ENCODING),
                ENCODING_NAME))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void fail_on_invalid_rule_parameter_type() {
        assertThatThrownBy(() -> load(IOUtils.toInputStream("<rules><rule><key>foo</key><name>Foo</name><param><key>key</key><type>INVALID</type></param></rule></rules>", ENCODING_NAME),
                ENCODING_NAME))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void fail_if_invalid_xml() {
        InputStream input = getClass().getResourceAsStream("RulesDefinitionXmlLoaderTest/invalid.xml");

        assertThatThrownBy(() -> load(input, ENCODING_NAME))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("XML is not valid");
    }

    @Test
    public void test_utf8_encoding() {
        InputStream input = getClass().getResourceAsStream("RulesDefinitionXmlLoaderTest/utf8.xml");
        RulesDefinition.Repository repository = load(input, ENCODING_NAME);

        assertThat(repository.rules()).hasSize(1);
        RulesDefinition.Rule rule = repository.rules().get(0);
        assertThat(rule.key()).isEqualTo("com.puppycrawl.tools.checkstyle.checks.naming.LocalVariableNameCheck");
        assertThat(rule.name()).isEqualTo("M & M");
        assertThat(rule.htmlDescription().charAt(0)).isEqualTo('\u00E9');
        assertThat(rule.htmlDescription().charAt(1)).isEqualTo('\u00E0');
        assertThat(rule.htmlDescription().charAt(2)).isEqualTo('\u0026');
    }

    @Test
    public void test_utf8_encoding_with_bom() {
        InputStream input = getClass().getResourceAsStream("RulesDefinitionXmlLoaderTest/utf8-with-bom.xml");
        RulesDefinition.Repository repository = load(input, ENCODING_NAME);

        assertThat(repository.rules()).hasSize(1);
        RulesDefinition.Rule rule = repository.rules().get(0);
        assertThat(rule.key()).isEqualTo("com.puppycrawl.tools.checkstyle.checks.naming.LocalVariableNameCheck");
        assertThat(rule.name()).isEqualTo("M & M");
        assertThat(rule.htmlDescription().charAt(0)).isEqualTo('\u00E9');
        assertThat(rule.htmlDescription().charAt(1)).isEqualTo('\u00E0');
        assertThat(rule.htmlDescription().charAt(2)).isEqualTo('\u0026');
    }

    @Test
    public void support_deprecated_format() {
        // the deprecated format uses some attributes instead of nodes
        InputStream input = getClass().getResourceAsStream("RulesDefinitionXmlLoaderTest/deprecated.xml");
        RulesDefinition.Repository repository = load(input, ENCODING_NAME);

        assertThat(repository.rules()).hasSize(1);
        RulesDefinition.Rule rule = repository.rules().get(0);
        assertThat(rule.key()).isEqualTo("org.sonar.it.checkstyle.MethodsCountCheck");
        assertThat(rule.internalKey()).isEqualTo("Checker/TreeWalker/org.sonar.it.checkstyle.MethodsCountCheck");
        assertThat(rule.severity()).isEqualTo(Severity.CRITICAL);
        assertThat(rule.htmlDescription()).isEqualTo("Count methods");
        assertThat(rule.param("minMethodsCount")).isNotNull();
    }

    @Test
    public void test_linear_remediation_function() {
        String xml = "" +
                "<rules>" +
                "  <rule>" +
                "    <key>1</key>" +
                "    <name>One</name>" +
                "    <description>Desc</description>" +

                "    <gapDescription>lines</gapDescription>" +
                "    <remediationFunction>LINEAR</remediationFunction>" +
                "    <remediationFunctionGapMultiplier>2d 3h</remediationFunctionGapMultiplier>" +
                "  </rule>" +
                "</rules>";
        RulesDefinition.Rule rule = load(xml).rule("1");
        assertThat(rule.gapDescription()).isEqualTo("lines");
        DebtRemediationFunction function = rule.debtRemediationFunction();
        assertThat(function).isNotNull();
        assertThat(function.type()).isEqualTo(DebtRemediationFunction.Type.LINEAR);
        assertThat(function.gapMultiplier()).isEqualTo("2d3h");
        assertThat(function.baseEffort()).isNull();
    }

    @Test
    public void test_linear_with_offset_remediation_function() {
        String xml = "" +
                "<rules>" +
                "  <rule>" +
                "    <key>1</key>" +
                "    <name>One</name>" +
                "    <description>Desc</description>" +

                "    <effortToFixDescription>lines</effortToFixDescription>" +
                "    <remediationFunction>LINEAR_OFFSET</remediationFunction>" +
                "    <remediationFunctionGapMultiplier>2d 3h</remediationFunctionGapMultiplier>" +
                "    <remediationFunctionBaseEffort>5min</remediationFunctionBaseEffort>" +
                "  </rule>" +
                "</rules>";
        RulesDefinition.Rule rule = load(xml).rule("1");
        assertThat(rule.gapDescription()).isEqualTo("lines");
        DebtRemediationFunction function = rule.debtRemediationFunction();
        assertThat(function).isNotNull();
        assertThat(function.type()).isEqualTo(DebtRemediationFunction.Type.LINEAR_OFFSET);
        assertThat(function.gapMultiplier()).isEqualTo("2d3h");
        assertThat(function.baseEffort()).isEqualTo("5min");
    }

    @Test
    public void test_constant_remediation_function() {
        String xml = "" +
                "<rules>" +
                "  <rule>" +
                "    <key>1</key>" +
                "    <name>One</name>" +
                "    <description>Desc</description>" +
                "    <remediationFunction>CONSTANT_ISSUE</remediationFunction>" +
                "    <remediationFunctionBaseEffort>5min</remediationFunctionBaseEffort>" +
                "  </rule>" +
                "</rules>";
        RulesDefinition.Rule rule = load(xml).rule("1");
        DebtRemediationFunction function = rule.debtRemediationFunction();
        assertThat(function).isNotNull();
        assertThat(function.type()).isEqualTo(DebtRemediationFunction.Type.CONSTANT_ISSUE);
        assertThat(function.gapMultiplier()).isNull();
        assertThat(function.baseEffort()).isEqualTo("5min");
    }

    @Test
    public void fail_if_invalid_remediation_function() {
        assertThatThrownBy(() -> load("" +
                "<rules>" +
                "  <rule>" +
                "    <key>1</key>" +
                "    <name>One</name>" +
                "    <description>Desc</description>" +
                "    <remediationFunction>UNKNOWN</remediationFunction>" +
                "  </rule>" +
                "</rules>"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Fail to load the rule with key [squid:1]")
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage("No enum constant org.sonar.api.server.debt.DebtRemediationFunction.Type.UNKNOWN");
    }

    @Test
    @SuppressWarnings({"removal"})
    public void markdown_description() {
        String xml = "" +
                "<rules>" +
                "  <rule>" +
                "    <key>1</key>" +
                "    <name>One</name>" +
                "    <description>Desc</description>" +
                "    <descriptionFormat>MARKDOWN</descriptionFormat>" +
                "  </rule>" +
                "</rules>";
        RulesDefinition.Rule rule = load(xml).rule("1");
        assertThat(rule.markdownDescription()).isEqualTo("Desc");
        assertThat(rule.htmlDescription()).isNull();
    }

    @Test
    public void fail_if_unsupported_description_format() {
        String xml = "" +
                "<rules>" +
                "  <rule>" +
                "    <key>1</key>" +
                "    <name>One</name>" +
                "    <description>Desc</description>" +
                "    <descriptionFormat>UNKNOWN</descriptionFormat>" +
                "  </rule>" +
                "</rules>";

        assertThatThrownBy(() -> load(xml).rule("1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Fail to load the rule with key [squid:1]")
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage("No enum constant org.sonar.plugins.pmd.rule.RulesDefinitionXmlLoader.DescriptionFormat.UNKNOWN");
    }

    @Test
    public void test_deprecated_remediation_function() {
        String xml = "" +
                "<rules>" +
                "  <rule>" +
                "    <key>1</key>" +
                "    <name>One</name>" +
                "    <description>Desc</description>" +
                "    <effortToFixDescription>lines</effortToFixDescription>" +
                "    <debtRemediationFunction>LINEAR_OFFSET</debtRemediationFunction>" +
                "    <debtRemediationFunctionCoefficient>2d 3h</debtRemediationFunctionCoefficient>" +
                "    <debtRemediationFunctionOffset>5min</debtRemediationFunctionOffset>" +
                "  </rule>" +
                "</rules>";
        RulesDefinition.Rule rule = load(xml).rule("1");
        assertThat(rule.gapDescription()).isEqualTo("lines");
        DebtRemediationFunction function = rule.debtRemediationFunction();
        assertThat(function).isNotNull();
        assertThat(function.type()).isEqualTo(DebtRemediationFunction.Type.LINEAR_OFFSET);
        assertThat(function.gapMultiplier()).isEqualTo("2d3h");
        assertThat(function.baseEffort()).isEqualTo("5min");
    }

    @Test
    public void test_filter_non_sonar_tags() {
        String xml = "" +
                "<rules>" +
                "<rule >" +
                "<key>rule-non-sonar-tags-test</key>" +
                "<name>Rule non Sonar Tags Test</name>" +
                "<description>Rule to check filtering of non Sonar tags</description>" +
                "<type>BUG</type>" +
                "<tag>tests</tag>" +
                "<tag>main-sources</tag>" +
                "</rule>" +
                "</rules>";

        RulesDefinition.Repository rulesRepo = load(xml);
        RulesDefinition.Rule nonSonarTags = rulesRepo.rule("rule-non-sonar-tags-test");

        assertThat(nonSonarTags.tags())
                .withFailMessage("Should not contain main-sources")
                .containsOnly("tests");

    }

    @Test
    public void test_analysis_scope() {
        String xml = "" +
                "<rules>" +
                "<rule >" +
                "<key>Rule00</key>" +
                "<name>Rule 00</name>" +
                "<description>Description of rule 00</description>" +
                "<type>BUG</type>" +
                "</rule>" +

                "<rule>" +
                "<key>Rule01</key>" +
                "<name>Test rule 01</name>" +
                "<description>Description</description>" +
                "<type>BUG</type>" +
                "</rule>" +

                "<rule>" +
                "<key>Rule02</key>" +
                "<name>Rule 02</name>" +
                "<description>Description</description>" +
                "<type>CODE_SMELL</type>" +
                "<tag>tests</tag>" +
                "</rule>" +

                "<rule>" +
                "<key>Rule03</key>" +
                "<name>Main rule 03</name>" +
                "<description>Description</description>" +
                "<type>CODE_SMELL</type>" +
                "<tag>main-sources</tag>" +
                "</rule>" +

                "<rule>" +
                "<key>Rule04</key>" +
                "<name>Rule 04</name>" +
                "<description>Description</description>" +
                "<type>CODE_SMELL</type>" +
                "<tag>main-sources</tag>" +
                "<tag>tests</tag>" +
                "</rule>" +

                "<rule>" +
                "<key>Rule05</key>" +
                "<name>Rule 05</name>" +
                "<description>Description</description>" +
                "<severity>MAJOR</severity>" +
                "<type>CODE_SMELL</type>" +
                "<tag>test</tag>" + // wrong tag so ALL
                "</rule>" +

                "<rule>" +
                "<key>Rule06</key>" +
                "<name>Rule Not Test But Main 06</name>" +
                "<description>Description</description>" +
                "<severity>MAJOR</severity>" +
                "<type>CODE_SMELL</type>" +
                "<tag>main-sources</tag>" + // override the name containing Test, make it Main
                "</rule>" +

                "<rule>" +
                "<key>Rule07</key>" +
                "<name>Rule Not Test But All 07</name>" +
                "<description>Description</description>" +
                "<severity>MAJOR</severity>" +
                "<type>CODE_SMELL</type>" +
                "<tag>main-sources</tag>" + // override the name containing Test
                "<tag>tests</tag>" + // make it both, main-sources and test -> All
                "</rule>" +

                "<rule>" +
                "<key>Rule08</key>" +
                "<name>Rule for junit</name>" +
                "<description>Description</description>" +
                "<severity>MAJOR</severity>" +
                "<type>CODE_SMELL</type>" +
                "</rule>" +
                "</rules>";
        RulesDefinition.Repository rulesRepo = load(xml);
        RulesDefinition.Rule rule0 = rulesRepo.rule("Rule00");
        assertThat(rule0.scope()).isEqualTo(RuleScope.ALL);
        RulesDefinition.Rule rule1 = rulesRepo.rule("Rule01");
        assertThat(rule1.scope()).isEqualTo(RuleScope.TEST);
        RulesDefinition.Rule rule2 = rulesRepo.rule("Rule02");
        assertThat(rule2.scope()).isEqualTo(RuleScope.TEST);
        RulesDefinition.Rule rule3 = rulesRepo.rule("Rule03");
        assertThat(rule3.scope()).isEqualTo(RuleScope.MAIN);
        RulesDefinition.Rule rule4 = rulesRepo.rule("Rule04");
        assertThat(rule4.scope()).isEqualTo(RuleScope.ALL);
        RulesDefinition.Rule rule5 = rulesRepo.rule("Rule05");
        assertThat(rule5.scope()).isEqualTo(RuleScope.ALL);
        RulesDefinition.Rule rule6 = rulesRepo.rule("Rule06");
        assertThat(rule6.scope()).isEqualTo(RuleScope.MAIN);
        RulesDefinition.Rule rule7 = rulesRepo.rule("Rule07");
        assertThat(rule7.scope()).isEqualTo(RuleScope.ALL);
        RulesDefinition.Rule rule8 = rulesRepo.rule("Rule08");
        assertThat(rule8.scope()).isEqualTo(RuleScope.TEST);
    }

    private RulesDefinition.Repository load(InputStream input, String encoding) {
        RulesDefinition.Context context = new RulesDefinitionContext();
        RulesDefinition.NewRepository newRepository = context.createRepository("squid", "java");
        underTest.load(newRepository, input, encoding);
        newRepository.done();
        return context.repository("squid");
    }

    private RulesDefinition.Repository load(String xml) {
        RulesDefinition.Context context = new RulesDefinitionContext();
        RulesDefinition.NewRepository newRepository = context.createRepository("squid", "java");
        underTest.load(newRepository, new StringReader(xml));
        newRepository.done();
        return context.repository("squid");
    }
}

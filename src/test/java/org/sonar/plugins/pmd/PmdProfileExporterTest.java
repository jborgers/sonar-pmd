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

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import org.fest.assertions.Condition;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Param;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.pmd.xml.PmdProperty;
import org.sonar.plugins.pmd.xml.PmdRule;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PmdProfileExporterTest {

  private static final CharMatcher EOLS = CharMatcher.anyOf("\n\r");

  PmdProfileExporter exporter = new PmdProfileExporter();

  @org.junit.Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void should_export_pmd_profile_on_writer() {
    String importedXml = PmdTestUtils.getResourceContent("/org/sonar/plugins/pmd/export_simple.xml");

    StringWriter stringWriter = new StringWriter();
    exporter.exportProfile(importProfile(importedXml), stringWriter);

    assertThat(stringWriter.toString()).satisfies(equalsIgnoreEOL(importedXml));
  }

  @Test
  public void should_export_pmd_profile_on_writer_exception() throws IOException {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("An exception occurred while generating the PMD configuration file from profile: null");

    String importedXml = PmdTestUtils.getResourceContent("/org/sonar/plugins/pmd/export_simple.xml");

    Writer writer = mock(Writer.class);
    doThrow(new IOException("test exception")).when(writer).write(anyString());
    exporter.exportProfile(importProfile(importedXml), writer);
  }

  @Test
  public void should_export_pmd_profile() {
    String importedXml = PmdTestUtils.getResourceContent("/org/sonar/plugins/pmd/export_simple.xml");

    String exportedXml = exporter.exportProfile(PmdConstants.REPOSITORY_KEY, importProfile(importedXml));

    assertThat(exportedXml).satisfies(equalsIgnoreEOL(importedXml));
  }

  @Test
  public void should_export_empty_configuration_as_xml() {
    String exportedXml = exporter.exportProfile(PmdConstants.REPOSITORY_KEY, RulesProfile.create());

    assertThat(exportedXml).satisfies(equalsIgnoreEOL("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ruleset />"));
  }

  @Test
  public void should_export_xPath_rule() {
    Rule rule = Rule.create(PmdConstants.REPOSITORY_KEY, "MyOwnRule", "This is my own xpath rule.")
      .setConfigKey(PmdConstants.XPATH_CLASS)
      .setRepositoryKey(PmdConstants.REPOSITORY_KEY);
    rule.createParameter(PmdConstants.XPATH_EXPRESSION_PARAM);
    rule.createParameter(PmdConstants.XPATH_MESSAGE_PARAM);

    RulesProfile profile = RulesProfile.create();
    ActiveRule xpath = profile.activateRule(rule, null);
    xpath.setParameter(PmdConstants.XPATH_EXPRESSION_PARAM, "//FieldDeclaration");
    xpath.setParameter(PmdConstants.XPATH_MESSAGE_PARAM, "This is bad");

    String exportedXml = exporter.exportProfile(PmdConstants.REPOSITORY_KEY, profile);

    assertThat(exportedXml).satisfies(equalsIgnoreEOL(PmdTestUtils.getResourceContent("/org/sonar/plugins/pmd/export_xpath_rules.xml")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_fail_if_message_not_provided_for_xPath_rule() {
    PmdRule rule = new PmdRule(PmdConstants.XPATH_CLASS);

    rule.addProperty(new PmdProperty(PmdConstants.XPATH_EXPRESSION_PARAM, "xpathExpression"));
    rule.setName("MyOwnRule");

    exporter.processXPathRule("xpathKey", rule);
  }

  @Test
  public void should_process_xPath_rule() {
    PmdRule rule = new PmdRule(PmdConstants.XPATH_CLASS);
    rule.setName("MyOwnRule");
    rule.addProperty(new PmdProperty(PmdConstants.XPATH_EXPRESSION_PARAM, "xpathExpression"));
    rule.addProperty(new PmdProperty(PmdConstants.XPATH_MESSAGE_PARAM, "message"));

    exporter.processXPathRule("xpathKey", rule);

    assertThat(rule.getMessage()).isEqualTo("message");
    assertThat(rule.getRef()).isNull();
    assertThat(rule.getClazz()).isEqualTo(PmdConstants.XPATH_CLASS);
    assertThat(rule.getProperty(PmdConstants.XPATH_MESSAGE_PARAM)).isNull();
    assertThat(rule.getName()).isEqualTo("xpathKey");
    assertThat(rule.getProperty(PmdConstants.XPATH_EXPRESSION_PARAM).getValue()).isEqualTo("xpathExpression");
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_fail_if_xPath_not_provided() {
    PmdRule rule = new PmdRule(PmdConstants.XPATH_CLASS);
    rule.setName("MyOwnRule");
    rule.addProperty(new PmdProperty(PmdConstants.XPATH_MESSAGE_PARAM, "This is bad"));

    exporter.processXPathRule("xpathKey", rule);
  }

  static RulesProfile importProfile(String configuration) {
    PmdRulesDefinition definition = new PmdRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
    RulesDefinition.Repository repository = context.repository(PmdConstants.REPOSITORY_KEY);
    RuleFinder ruleFinder = createRuleFinder(repository.rules());
    PmdProfileImporter importer = new PmdProfileImporter(ruleFinder);

    return importer.importProfile(new StringReader(configuration), ValidationMessages.create());
  }

  static RuleFinder createRuleFinder(final List<RulesDefinition.Rule> rules) {
    RuleFinder ruleFinder = mock(RuleFinder.class);
    final List<Rule> convertedRules = convert(rules);

    when(ruleFinder.find(any(RuleQuery.class))).then(new Answer<Rule>() {
      @Override
      public Rule answer(InvocationOnMock invocation) {
        RuleQuery query = (RuleQuery) invocation.getArguments()[0];
        for (Rule rule : convertedRules) {
          if (query.getConfigKey().equals(rule.getConfigKey())) {
            return rule;
          }
        }
        return null;
      }
    });
    return ruleFinder;
  }

  private static List<Rule> convert(List<RulesDefinition.Rule> rules) {
    List<Rule> results = Lists.newArrayListWithCapacity(rules.size());
    for (RulesDefinition.Rule rule : rules) {
      Rule newRule = Rule.create(rule.repository().key(), rule.key(), rule.name())
        .setDescription(rule.htmlDescription())
        .setRepositoryKey(rule.repository().key())
        .setConfigKey(rule.internalKey());
      if (!rule.params().isEmpty()) {
        List<RuleParam> ruleParams = Lists.newArrayList();
        for (Param param : rule.params()) {
          ruleParams.add(new RuleParam().setDefaultValue(param.defaultValue()).setKey(param.name()));
        }
        newRule.setParams(ruleParams);
      }
      results.add(newRule);
    }
    return results;
  }

  public static Condition<String> equalsIgnoreEOL(String text) {
    final String strippedText = EOLS.removeFrom(text);

    return new Condition<String>() {
      @Override
      public boolean matches(String value) {
        return EOLS.removeFrom(value).equals(strippedText);
      }
    }.as("equal to " + strippedText);
  }
}

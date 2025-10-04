package org.sonar.plugins.pmd.rule;

import org.junit.jupiter.api.Test;
import org.sonar.api.rule.RuleScope;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class PmdRuleScopeRegistryTest {

    @Test
    void parses_rule_scopes_from_attribute_key_and_alternative_elements() {
        URL url = getClass().getResource("/org/sonar/plugins/pmd/rule/test-jpinpoint.xml");
        assertThat(url).as("test resource should exist").isNotNull();

        PmdRuleScopeRegistry registry = new PmdRuleScopeRegistry();
        registry.addXmlUrls(url);

        // JP0001 has <title>, <name> containing 'Test' and <tag>tests</tag>
        assertThat(registry.getScope("JP0001")).isEqualTo(RuleScope.TEST);
        // JP0002 has <tags>main-sources, performance</tags>
        assertThat(registry.getScope("JP0002")).isEqualTo(RuleScope.MAIN);
    }

    @Test
    void parses_sample_jpinpoint_xml_with_nested_cdata_descriptions() {
        URL url = getClass().getResource("/org/sonar/plugins/pmd/rule/test-jpinpoint-sample.xml");
        assertThat(url).as("sample test resource should exist").isNotNull();

        PmdRuleScopeRegistry registry = new PmdRuleScopeRegistry();
        registry.addXmlUrls(url);

        // Both rules don't have explicit test/main tags; default scope should fall back to ALL
        assertThat(registry.getScope("AvoidCDIReferenceLeak")).isEqualTo(RuleScope.ALL);
        assertThat(registry.getScope("AvoidCalendar")).isEqualTo(RuleScope.ALL);
    }

    @Test
    void parses_rules_with_default_namespace_and_no_xml_header() {
        URL url = getClass().getResource("/org/sonar/plugins/pmd/rule/test-rules-default-ns-no-header.xml");
        assertThat(url).as("resource with default namespace & no header should exist").isNotNull();

        PmdRuleScopeRegistry registry = new PmdRuleScopeRegistry();
        registry.addXmlUrls(url);

        assertThat(registry.getScope("NS001")).isEqualTo(RuleScope.TEST);
        assertThat(registry.getScope("NS002")).isEqualTo(RuleScope.MAIN);
    }

    @Test
    void parses_rules_with_prefixed_namespace_and_with_header() {
        URL url = getClass().getResource("/org/sonar/plugins/pmd/rule/test-rules-prefixed-ns-with-header.xml");
        assertThat(url).as("resource with prefixed namespace & header should exist").isNotNull();

        PmdRuleScopeRegistry registry = new PmdRuleScopeRegistry();
        registry.addXmlUrls(url);

        assertThat(registry.getScope("NSP001")).isEqualTo(RuleScope.TEST);
        assertThat(registry.getScope("NSP002")).isEqualTo(RuleScope.MAIN);
    }
}

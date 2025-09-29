/*
 * SonarQube PMD7 Plugin
 */
package org.sonar.plugins.pmd.rule.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleParamFormatterTest {

    @Test
    void buildDescription_removesExistingAllowedValues_andAppendsStandardized_single() {
        String existing = "Some description. Allowed values: [x, y]. Extra.";
        String fromPropInfo = null;
        List<String> accepted = Arrays.asList("A","B","C");

        String result = RuleParamFormatter.buildDescription(existing, fromPropInfo, accepted, false);

        assertThat(result)
                .isEqualTo("Some description. Extra. Allowed values: [A,B,C]. Select one of the values.");
    }

    @Test
    void buildDescription_addsPunctuation_ifMissing_multiple() {
        String existing = "Pick values"; // no punctuation
        String fromPropInfo = "ignored";
        List<String> accepted = Arrays.asList("x","y");

        String result = RuleParamFormatter.buildDescription(existing, fromPropInfo, accepted, true);

        assertThat(result)
                .isEqualTo("Pick values. Allowed values: [x,y]. Select one or more values.");
    }

    @Test
    void buildDescription_usesPropInfo_whenExistingBlank_and_noSelect() {
        String existing = "   ";
        String fromPropInfo = "Base description";

        String result = RuleParamFormatter.buildDescription(existing, fromPropInfo, null, false);

        assertThat(result).isEqualTo("Base description");
    }

    @Test
    void buildSelectTypeToken_singleAndMultiple() {
        List<String> accepted = Arrays.asList("a","b,c","d\"e");

        String single = RuleParamFormatter.buildSelectTypeToken(accepted, false);
        String multi = RuleParamFormatter.buildSelectTypeToken(accepted, true);

        assertThat(single)
                .isEqualTo("SINGLE_SELECT_LIST,values=\"a,b,c,d\"\"e\"");
        assertThat(multi)
                .isEqualTo("SINGLE_SELECT_LIST,multiple=true,values=\"a,b,c,d\"\"e\"");
    }
}

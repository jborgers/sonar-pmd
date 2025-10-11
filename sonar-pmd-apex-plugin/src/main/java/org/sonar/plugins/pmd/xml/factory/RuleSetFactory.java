package org.sonar.plugins.pmd.xml.factory;

import org.sonar.plugins.pmd.xml.PmdRuleSet;

import java.io.IOException;

public interface RuleSetFactory extends AutoCloseable {
    PmdRuleSet create();

    @Override
    void close() throws IOException;
}

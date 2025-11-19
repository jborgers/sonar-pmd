/*
 * Shared RuleSetFactory interface in lib.
 */
package org.sonar.plugins.pmd.xml.factory;

import java.io.Closeable;

import org.sonar.plugins.pmd.xml.PmdRuleSet;

public interface RuleSetFactory extends Closeable {

    PmdRuleSet create();
}

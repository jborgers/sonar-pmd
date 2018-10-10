/*
 * Java :: IT :: Plugins :: PMD Extension Plugin
 * Copyright (C) 2013-2018 SonarSource SA
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
package org.sonar.examples.pmd;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBody;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.IntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MaximumMethodsCountCheck extends AbstractJavaRule {

  private static final Logger LOG = LoggerFactory.getLogger(MaximumMethodsCountCheck.class);

  public static final IntegerProperty propertyDescriptor = new IntegerProperty("maxAuthorisedMethodsCount", 
    "Maximum number of methods authorised", 1, Integer.MAX_VALUE, 2, 1.0f);

  public MaximumMethodsCountCheck() {
    definePropertyDescriptor(propertyDescriptor);
  }

  @Override
  public void start(RuleContext ctx) {
    LOG.info("Start " + getName());
  }

  @Override
  public void end(RuleContext ctx) {
    LOG.info("End " + getName());
  }

  @Override
  public Object visit(ASTClassOrInterfaceBody node, Object data) {
    List<ASTMethodDeclaration> methods =  node.findDescendantsOfType(ASTMethodDeclaration.class);
    if (methods.size() > getProperty(propertyDescriptor)) {
      addViolation(data, node);
    }
    return super.visit(node, data);
  }

}

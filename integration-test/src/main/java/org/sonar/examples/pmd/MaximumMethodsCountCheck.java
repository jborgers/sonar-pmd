/*
 * SonarQube PMD7 Plugin Integration Test
 * Copyright (C) 2013-2021 SonarSource SA and others
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
package org.sonar.examples.pmd;


import java.util.List;


import net.sourceforge.pmd.lang.java.ast.ASTClassBody;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.NumericConstraints;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.reporting.RuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaximumMethodsCountCheck extends AbstractJavaRule {

    static {
        System.err.println("MaximumMethodsCountCheck loaded");
    }
    private static final Logger LOG = LoggerFactory.getLogger(MaximumMethodsCountCheck.class);

    private static final PropertyDescriptor<Integer> propertyDescriptor = PropertyFactory.intProperty("maxAuthorisedMethodsCount")
            .desc("Maximum number of methods authorised")
            .require(NumericConstraints.positive())
            .defaultValue(2)
            .build();

    public MaximumMethodsCountCheck() {
        definePropertyDescriptor(propertyDescriptor);
    }

    @Override
    public Object visit(ASTClassBody node, Object data) {
        LOG.info("Start {}", getName());
        throw new IllegalStateException("This rule should not be executed");
//        List<ASTMethodDeclaration> methods = node.descendants(ASTMethodDeclaration.class).toList();
//        if (methods.size() > getProperty(propertyDescriptor)) {
//            asCtx(data).addViolation(node);
//        }
//        LOG.info("End {}", getName());
//        return super.visit(node, data);
    }
}

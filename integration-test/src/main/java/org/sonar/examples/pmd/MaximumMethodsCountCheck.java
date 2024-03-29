/*
 * SonarQube PMD Plugin Integration Test
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

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBody;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.constraints.NumericConstraints;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class MaximumMethodsCountCheck extends AbstractJavaRule {

    private static final Logger LOG = Loggers.get(MaximumMethodsCountCheck.class);

    private static final PropertyDescriptor<Integer> propertyDescriptor = PropertyFactory.intProperty("maxAuthorisedMethodsCount")
            .desc("Maximum number of methods authorised")
            .require(NumericConstraints.positive())
            .defaultValue(2)
            .build();


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
        List<ASTMethodDeclaration> methods = node.findDescendantsOfType(ASTMethodDeclaration.class);
        if (methods.size() > getProperty(propertyDescriptor)) {
            asCtx(data).addViolation(node);
        }
        return super.visit(node, data);
    }
}

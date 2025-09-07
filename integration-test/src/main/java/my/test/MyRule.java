package my.test;

import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MyRule extends AbstractJavaRule {

    private static final Logger LOG = LoggerFactory.getLogger(MyRule.class);

    public MyRule() {
        super();
    }

    @Override
    public Object visit(ASTVariableId node, Object data) {
        LOG.info("Start {}", getName());
        return data;
    }
}
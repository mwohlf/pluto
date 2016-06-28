package net.wohlfart.pluto.scene.lang;

import org.antlr.v4.runtime.ParserRuleContext;

public class EvalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EvalException(String cause) {
        super("eval error: " + cause);
    }

    public EvalException(ParserRuleContext ctx) {
        this("illegal expression: " + ctx.getText(), ctx);
    }

    public EvalException(String msg, ParserRuleContext ctx) {
        super(msg + " at line:" + ctx.start.getLine());
    }

}

package net.wohlfart.pluto.scene.lang;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.stage.SceneLanguageBaseVisitor;
import net.wohlfart.pluto.stage.SceneLanguageParser.ParameterContext;

public class ParameterApplyVisitor<T> extends SceneLanguageBaseVisitor<T> {

    private static final Logger LOGGER = LoggerService.forClass(BehaviorVisitor.class);

    protected IFactoryDecorator<T> factory;

    protected T entity;

    private final EvalVisitor visitor;

    public ParameterApplyVisitor(EvalVisitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public T visitParameter(ParameterContext ctx) {
        // calculate the parameter value by reusing the eval visitor
        final Value<?> value = visitor.visit(ctx.expression());
        // assign by identifier if possible
        final TerminalNode identifier = ctx.Identifier();
        if (identifier != null) {
            final String type = identifier.getText();
            LOGGER.info("<visitProperty> call by type: " + type + " to " + value);
            factory.setTypedValue(entity, type, value);
            return entity;
        }

        return entity; // not used
    }

}

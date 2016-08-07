package net.wohlfart.pluto.scene.lang;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.stage.SceneLanguageBaseVisitor;
import net.wohlfart.pluto.stage.SceneLanguageParser.ParameterContext;

public class ParameterApplyVisitor<T> extends SceneLanguageBaseVisitor<T> {

    private static final Logger LOGGER = LoggerService.forClass(ParameterApplyVisitor.class);

    private IFactoryDecorator<T> factory;

    private T entity;

    private final EvalVisitor visitor;

    public ParameterApplyVisitor(EvalVisitor visitor) {
        this.visitor = visitor;
    }

    public ParameterApplyVisitor<T> withFactory(IFactoryDecorator<T> factory) {
        this.factory = factory;
        return this;
    }

    public ParameterApplyVisitor<T> withEntity(T entity) {
        this.entity = entity;
        return this;
    }

    @Override
    public T visitParameter(ParameterContext ctx) {
        try {
            return doVisitParameter(factory, entity, ctx);
        } catch (final Exception ex) {
            throw new EvalException(ex, ctx);
        }
    }

    T doVisitParameter(IFactoryDecorator<T> factory, T entity, ParameterContext ctx) throws Exception {
        // calculate the parameter value by reusing the eval visitor
        final Value<?> value = visitor.visit(ctx.expression());
        // assign by identifier if possible
        final TerminalNode identifier = ctx.Identifier();
        if (identifier == null) {
            throw new IllegalArgumentException("null identifier");
        }
        final String type = identifier.getText();
        if (type == null) {
            throw new IllegalArgumentException("null type for identifier: " + identifier);
        }

        factory.setTypedValue(entity, type, value);
        return entity;
    }

}

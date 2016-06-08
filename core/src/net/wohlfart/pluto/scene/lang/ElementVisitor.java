package net.wohlfart.pluto.scene.lang;

import java.util.Map;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.stage.SceneLanguageBaseVisitor;
import net.wohlfart.pluto.stage.SceneLanguageParser.ParameterContext;

public class ElementVisitor<T> extends SceneLanguageBaseVisitor<T> {

    private static final Logger LOGGER = LoggerService.forClass(BehaviorVisitor.class);

    private final ISceneGraph graph;

    private final Scope scope;

    private final Map<String, Function> functions;

    protected IFactoryDecorator<T> factory;

    protected T entity;

    public ElementVisitor(ISceneGraph graph, Scope scope, Map<String, Function> functions) {
        this.graph = graph;
        this.scope = scope;
        this.functions = functions;
    }

    @Override
    public T visitParameter(ParameterContext ctx) {

        // calculate the parameter value
        final Value<?> value = new EvalVisitor(graph, this.scope, functions).visit(ctx.expression());

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

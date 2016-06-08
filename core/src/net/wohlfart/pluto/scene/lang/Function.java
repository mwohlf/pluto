package net.wohlfart.pluto.scene.lang;

import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.stage.SceneLanguageParser.ExpressionContext;

public class Function {
    private final String id;
    private final List<TerminalNode> parameters;
    private final ParseTree parseTree;

    public Function(String id, List<TerminalNode> parameters, ParseTree parseTree) {
        this.id = id;
        this.parameters = parameters;
        this.parseTree = parseTree;
    }

    public Value invoke(List<ExpressionContext> params, ISceneGraph graph, Map<String, Function> functions, Scope parentScope) {
        if (params.size() != this.parameters.size()) {
            throw new RuntimeException("Illegal Function call, parameter count doesn't match,"
                    + " found " + params.size() + " params in call, function definition has " + params.size());
        }
        final Scope scope = new Scope(parentScope); // create function scope
        final EvalVisitor evalVisitor = new EvalVisitor(graph, scope, functions);
        for (int i = 0; i < this.parameters.size(); i++) {
            final Value value = evalVisitor.visit(params.get(i));
            scope.assignInScope(this.parameters.get(i).getText(), value);
        }
        return evalVisitor.visit(this.parseTree);
    }
}

package net.wohlfart.pluto.scene.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.ai.btree.Looping;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.stage.SceneLanguageBaseVisitor;
import net.wohlfart.pluto.stage.SceneLanguageLexer;
import net.wohlfart.pluto.stage.SceneLanguageParser;
import net.wohlfart.pluto.stage.SceneLanguageParser.AndAndExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.AndExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.AssertFunctionCallContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.AttributeContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.BlockContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.ConstantExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.DivideExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.ExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.ExpressionExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.ForStatementContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.FunctionCallExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.FunctionDeclarationContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.GtEqExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.GtExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.IdentifierFunctionCallContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.InExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.IncludeContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.InputExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.ListContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.ListExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.LtEqExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.LtExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.ModulusExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.MultiplyExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.NotExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.OrExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.OrOrExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.PowerExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.SizeFunctionCallContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.StatementContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.SubtractExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.TernaryExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.UnaryMinusExpressionContext;
import net.wohlfart.pluto.stage.SceneLanguageParser.WhileStatementContext;

/**
 * core class for interpreting scene definition
 */
public class EvalVisitor extends SceneLanguageBaseVisitor<Value> {

    private static final Logger LOGGER = LoggerService.forClass(EvalVisitor.class);

    private static final CharSequence COMMA = ".";

    private final ISceneGraph graph;

    private final Scope scope;

    private final Map<String, Function> functions;

    public EvalVisitor(ISceneGraph graph, Scope scope, Map<String, Function> functions) {
        this.graph = graph;
        this.scope = scope;
        this.functions = functions;
    }

    // functionDecl
    @Override
    public Value<?> visitFunctionDeclaration(FunctionDeclarationContext ctx) {
        // todo
        return Value.NULL;
    }

    // list: '[' exprList? ']'
    @Override
    public Value<?> visitList(ListContext ctx) {
        final List<Value<?>> list = new ArrayList<>();
        if (ctx.exprList() != null) {
            for (final ExpressionContext ex : ctx.exprList().expression()) {
                list.add(this.visit(ex));
            }
        }
        return Value.of(list);
    }

    // '-' expression                  @NotNull         #unaryMinusExpression
    @Override
    public Value<?> visitUnaryMinusExpression(UnaryMinusExpressionContext ctx) {
        final Value<?> value = this.visit(ctx.expression());
        if (value.isDouble()) {
            return Value.of(-1 * value.asDouble());
        } else if (value.isLong()) {
            return Value.of(-1 * value.asLong());
        }
        throw new EvalException(ctx);
    }

    // '!' expression                           #notExpression
    @Override
    public Value<?> visitNotExpression(NotExpressionContext ctx) {
        final Value<?> value = this.visit(ctx.expression());
        if (value.isBoolean()) {
            return Value.of(!value.asBoolean());
        }
        throw new EvalException(ctx);
    }

    // expression '^' expression                #powerExpression
    @Override
    public Value<?> visitPowerExpression(PowerExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        final Value<?> right = this.visit(ctx.expression(1));
        if (left.isNumber() && right.isNumber()) {
            return Value.of(Math.pow(left.asDouble(), right.asDouble()));
        }
        throw new EvalException(ctx);
    }

    // expression '*' expression                #multiplyExpression
    @Override
    public Value<?> visitMultiplyExpression(MultiplyExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        final Value<?> right = this.visit(ctx.expression(1));
        // number * number
        if (left.isDouble() && right.isDouble()) {
            return Value.of(left.asDouble() * right.asDouble());
        }
        // string * number
        if (left.isString() && right.isLong()) {
            final StringBuilder str = new StringBuilder();
            final long stop = right.asLong();
            for (int i = 0; i < stop; i++) {
                str.append(left.asString());
            }
            return Value.of(str.toString());
        }
        // list * number
        if (left.isList() && right.isLong()) {
            final List<Value<?>> total = new ArrayList<>();
            final long stop = right.asLong();
            for (int i = 0; i < stop; i++) {
                total.addAll(left.asList());
            }
            return Value.of(total);
        }
        throw new EvalException(ctx);
    }

    // expression '/' expression                #divideExpression
    @Override
    public Value<?> visitDivideExpression(DivideExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        final Value<?> right = this.visit(ctx.expression(1));
        if (left.isNumber() && right.isNumber()) {
            return Value.of(left.asDouble() / right.asDouble());
        }
        throw new EvalException(ctx);
    }

    // expression '%' expression                #modulusExpression
    @Override
    public Value<?> visitModulusExpression(ModulusExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        final Value<?> right = this.visit(ctx.expression(1));
        if (left.isDouble() && right.isDouble()) {
            return Value.of(left.asDouble() % right.asDouble());
        }
        throw new EvalException(ctx);
    }

    // expression '+' expression                #addExpression
    @Override
    public Value<?> visitAddExpression(SceneLanguageParser.AddExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        final Value<?> right = this.visit(ctx.expression(1));
        // number + number
        if (left.isNumber() && right.isNumber()) {
            return Value.of(
                    (left.isDouble() ? left.asDouble() : left.asLong())
                            + (right.isDouble() ? right.asDouble() : right.asLong()));
        }
        // list + any
        if (left.isList()) {
            final List<Value<?>> list = left.asList();
            list.add(right);
            return Value.of(list);
        }
        // string + any
        if (left.isString() || right.isString()) {
            return Value.of(left.asString() + right.toString());
        }
        throw new EvalException(ctx);
    }

    // expression '-' expression                #subtractExpression
    @Override
    public Value<?> visitSubtractExpression(SubtractExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        final Value<?> right = this.visit(ctx.expression(1));
        // number - number
        if (left.isNumber() && right.isNumber()) {
            return Value.of(
                    (left.isDouble() ? left.asDouble() : left.asLong())
                            - (right.isDouble() ? right.asDouble() : right.asLong()));
        }
        // remove element
        if (left.isList()) {
            final List<Value<?>> list = left.asList();
            list.remove(right);
            return Value.of(list);
        }
        throw new EvalException(ctx);
    }

    // expression '>=' expression               #gtEqExpression
    @Override
    public Value<?> visitGtEqExpression(GtEqExpressionContext ctx) {
        final Value left = this.visit(ctx.expression(0));
        final Value right = this.visit(ctx.expression(1));
        return Value.of(left.compareTo(right) >= 0);
    }

    // expression '<=' expression               #ltEqExpression
    @Override
    public Value<?> visitLtEqExpression(LtEqExpressionContext ctx) {
        final Value left = this.visit(ctx.expression(0));
        final Value right = this.visit(ctx.expression(1));
        return Value.of(left.compareTo(right) <= 0);
    }

    // expression '>' expression                #gtExpression
    @Override
    public Value<?> visitGtExpression(GtExpressionContext ctx) {
        final Value left = this.visit(ctx.expression(0));
        final Value right = this.visit(ctx.expression(1));
        return Value.of(left.compareTo(right) > 0);
    }

    // expression '<' expression                #ltExpression
    @Override
    public Value<?> visitLtExpression(LtExpressionContext ctx) {
        final Value left = this.visit(ctx.expression(0));
        final Value right = this.visit(ctx.expression(1));
        return Value.of(left.compareTo(right) < 0);
    }

    // expression '==' expression               #eqExpression
    @Override
    public Value<?> visitEqExpression(SceneLanguageParser.EqExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        final Value<?> right = this.visit(ctx.expression(1));
        return Value.of(left.equals(right));
    }

    // expression '!=' expression               #notEqExpression
    @Override
    public Value<?> visitNotEqExpression(SceneLanguageParser.NotEqExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        final Value<?> right = this.visit(ctx.expression(1));
        return Value.of(!left.equals(right));
    }

    // expression '&&' expression               #andAndExpression
    @Override
    public Value<?> visitAndAndExpression(AndAndExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        if (left.isBoolean()) {
            if (!left.asBoolean()) {
                return Value.of(false); // shortcut eval
            } else {
                final Value<?> right = this.visit(ctx.expression(1));
                return Value.of(right);
            }
        }
        throw new EvalException(ctx);
    }

    // expression '||' expression               #orOrExpression
    @Override
    public Value<?> visitOrOrExpression(OrOrExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        if (left.isBoolean()) {
            if (left.asBoolean()) {
                return Value.of(true); // shortcut eval
            } else {
                final Value<?> right = this.visit(ctx.expression(1));
                return Value.of(right);
            }
        }
        throw new EvalException(ctx);
    }

    // expression '&' expression               #andExpression
    @Override
    public Value<?> visitAndExpression(AndExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        final Value<?> right = this.visit(ctx.expression(1));
        if (left.isBoolean() && right.isBoolean()) {
            return Value.of(left.asBoolean() && right.asBoolean());
        }
        if (left.isBehavior() && right.isBehavior()) {
            return left.sequential(right);
        }
        throw new EvalException(ctx);
    }

    // expression '|' expression               #orExpression
    @Override
    public Value<?> visitOrExpression(OrExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        final Value<?> right = this.visit(ctx.expression(1));
        if (left.isBoolean() && right.isBoolean()) {
            return Value.of(left.asBoolean() || right.asBoolean());
        }
        if (left.isBehavior() && right.isBehavior()) {
            return left.parallel(right);
        }
        throw new EvalException(ctx);
    }

    // expression '?' expression ':' expression #ternaryExpression
    @Override
    public Value<?> visitTernaryExpression(TernaryExpressionContext ctx) {
        final Value<?> condition = this.visit(ctx.expression(0));
        if (condition.asBoolean()) {
            return Value.of(this.visit(ctx.expression(1)));
        } else {
            return Value.of(this.visit(ctx.expression(2)));
        }
    }

    // expression In expression                 #inExpression
    @Override
    public Value<?> visitInExpression(InExpressionContext ctx) {
        final Value<?> left = this.visit(ctx.expression(0));
        final Value<?> right = this.visit(ctx.expression(1));
        if (right.isList()) {
            for (final Object val : right.asList()) {
                if (val.equals(left)) {
                    return Value.of(true);
                }
            }
            return Value.of(false);
        }
        throw new EvalException(ctx);
    }

    // Identifier '++'                         #postIncrement
    @Override
    public Value<?> visitPostIncrement(SceneLanguageParser.PostIncrementContext ctx) {
        final String identifier = ctx.Identifier().getText();
        final Value<?> value = scope.resolve(identifier);
        scope.assignExisting(identifier, Value.of(value.asLong() + 1));
        return value;
    }

    // Number                                   #numberExpression
    @Override
    public Value<?> visitNumberExpression(SceneLanguageParser.NumberExpressionContext ctx) {
        final String number = ctx.getText();
        if (ctx.getText().contains(COMMA)) {
            return Value.of(Double.valueOf(number));
        } else {
            return Value.of(Long.valueOf(number));
        }
    }

    // Bool                                     #boolExpression
    @Override
    public Value<?> visitBoolExpression(SceneLanguageParser.BoolExpressionContext ctx) {
        return Value.of(Boolean.valueOf(ctx.getText()));
    }

    // Null                                     #nullExpression
    @Override
    public Value<?> visitNullExpression(SceneLanguageParser.NullExpressionContext ctx) {
        return Value.NULL;
    }

    private Value<?> resolveIndexes(ParserRuleContext ctx, Value<?> value, List<ExpressionContext> indices) {
        for (final ExpressionContext ec : indices) {
            final Value<?> idx = this.visit(ec);
            if (!value.isList() && !value.isString()) {
                throw new EvalException("Problem resolving indexes on " + value + " at " + idx, ec);
            }
            final int i = idx.asDouble().intValue();
            if (value.isString()) {
                value = Value.of(value.asString().substring(i, i + 1));
            } else {
                value = value.asList().get(i);
            }
        }
        return value;
    }

    private void setAtIndex(ParserRuleContext ctx, List<ExpressionContext> indices, Value<?> value, Value<?> newValue) {
        if (!value.isList()) {
            throw new EvalException(ctx);
        }
        // TODO some more list size checking in here
        for (int i = 0; i < indices.size() - 1; i++) {
            final Value<?> idx = this.visit(indices.get(i));
            if (!idx.isDouble()) {
                throw new EvalException(ctx);
            }
            value = value.asList().get(idx.asDouble().intValue());
        }
        final Value<?> idx = this.visit(indices.get(indices.size() - 1));
        if (!idx.isDouble()) {
            throw new EvalException(ctx);
        }
        value.asList().set(idx.asDouble().intValue(), newValue);
    }

    // functionCall indexes?                    #functionCallExpression
    @Override
    public Value<?> visitFunctionCallExpression(FunctionCallExpressionContext ctx) {
        Value<?> value = this.visit(ctx.functionCall());
        if (ctx.indexes() != null) {
            final List<ExpressionContext> expressions = ctx.indexes().expression();
            value = resolveIndexes(ctx, value, expressions);
        }
        return value;
    }

    // list indexes?                            #listExpression
    @Override
    public Value<?> visitListExpression(ListExpressionContext ctx) {
        Value<?> value = this.visit(ctx.list());
        if (ctx.indexes() != null) {
            final List<ExpressionContext> expressions = ctx.indexes().expression();
            value = resolveIndexes(ctx, value, expressions);
        }
        return value;
    }

    // Identifier indexes?                      #identifierExpression
    @Override
    @Nonnull
    public Value<?> visitIdentifierExpression(SceneLanguageParser.IdentifierExpressionContext ctx) {
        final String id = ctx.Identifier().getText();
        Value<?> value = scope.resolve(id);
        if (value == null) {
            throw new EvalException("value for identifier '" + id + "' not found in current scope ", ctx);
        }

        if (ctx.indexes() != null) {
            final List<ExpressionContext> expressions = ctx.indexes().expression();
            value = resolveIndexes(ctx, value, expressions);
        }
        return value;
    }

    // String indexes?                          #stringExpression
    @Override
    public Value<?> visitStringExpression(SceneLanguageParser.StringExpressionContext ctx) {
        String text = ctx.getText();
        text = text.substring(1, text.length() - 1); //.replaceAll("\\\\(.)", "$1");
        Value<?> value = Value.of(text);
        if (ctx.indexes() != null) {
            final List<ExpressionContext> exexpressions = ctx.indexes().expression();
            value = resolveIndexes(ctx, value, exexpressions);
        }
        return value;
    }

    // '(' expression ')' indexes?              #expressionExpression
    @Override
    public Value<?> visitExpressionExpression(ExpressionExpressionContext ctx) {
        final Value<?> value = this.visit(ctx.expression());
        if (ctx.indexes() != null) {
            final List<ExpressionContext> exps = ctx.indexes().expression();
            return resolveIndexes(ctx, value, exps);
        }
        if (value.isBehavior()) {
            final Looping looping = new Looping();
            looping.addChild(value.asBehavior());
            return Value.of(looping);
        }
        return value;
    }

    // Input '(' String? ')'                    #inputExpression
    @Override
    public Value<?> visitInputExpression(InputExpressionContext ctx) {
        final TerminalNode inputString = ctx.String();
        try {
            if (inputString != null) {
                String text = inputString.getText();
                text = text.substring(1, text.length() - 1).replaceAll("\\\\(.)", "$1");
                return Value.of(new String(Files.readAllBytes(Paths.get(text))));
            } else {
                // TODO try/catch:
                final BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
                return Value.of(buffer.readLine());
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    // assignment
    // : Identifier indexes? '=' expression
    // ;
    @Override
    public Value<?> visitAssign(SceneLanguageParser.AssignContext ctx) {
        final Value<?> right = this.visit(ctx.expression());
        if (ctx.indexes() != null) {
            final Value<?> value = scope.resolve(ctx.Identifier().getText());
            final List<ExpressionContext> expressions = ctx.indexes().expression();
            setAtIndex(ctx, expressions, value, right);
        } else {
            final String id = ctx.Identifier().getText();
            scope.assign(id, right);
        }
        return Value.NULL;
    }

    @Override
    public Value<?> visitAssignMinus(SceneLanguageParser.AssignMinusContext ctx) {
        final Value<?> right = this.visit(ctx.expression());
        final String identifier = ctx.Identifier().getText();
        final Value<?> result = scope.resolve(identifier).minus(right);
        scope.assignExisting(identifier, result);
        return result;
    }

    @Override
    public Value<?> visitAssignPlus(SceneLanguageParser.AssignPlusContext ctx) {
        final Value<?> right = this.visit(ctx.expression());
        final String identifier = ctx.Identifier().getText();
        final Value<?> result = scope.resolve(identifier).plus(right);
        scope.assignExisting(identifier, result);
        return result;
    }

    // Identifier '(' exprList? ')' #identifierFunctionCall
    @Override
    public Value<?> visitIdentifierFunctionCall(IdentifierFunctionCallContext ctx) {
        final List<ExpressionContext> params = ctx.exprList() != null ? ctx.exprList().expression() : new ArrayList<ExpressionContext>();
        final String id = ctx.Identifier().getText() + params.size();
        Function function;
        if ((function = functions.get(id)) != null) {
            return function.invoke(params, graph, functions, scope);
        }
        throw new EvalException(ctx);
    }

    // Println '(' expression? ')'  #printlnFunctionCall
    @Override
    public Value<?> visitPrintlnFunctionCall(SceneLanguageParser.PrintlnFunctionCallContext ctx) {
        System.out.println(this.visit(ctx.expression()).asString());
        return Value.NULL;
    }

    // Print '(' expression ')'     #printFunctionCall
    @Override
    public Value<?> visitPrintFunctionCall(SceneLanguageParser.PrintFunctionCallContext ctx) {
        System.out.print(this.visit(ctx.expression()).asString());
        return Value.NULL;
    }

    // Assert '(' expression ')'    #assertFunctionCall
    @Override
    public Value<?> visitAssertFunctionCall(AssertFunctionCallContext ctx) {
        final Value<?> value = this.visit(ctx.expression());

        if (!value.isBoolean()) {
            throw new EvalException(ctx);
        }

        if (!value.asBoolean()) {
            throw new AssertionError("Failed Assertion " + ctx.expression().getText() + " line:" + ctx.start.getLine());
        }

        return Value.NULL;
    }

    // Size '(' expression ')'      #sizeFunctionCall
    @Override
    public Value<?> visitSizeFunctionCall(SizeFunctionCallContext ctx) {
        final Value<?> value = this.visit(ctx.expression());

        if (value.isString()) {
            return Value.of(value.asString().length());
        }

        if (value.isList()) {
            return Value.of(value.asList().size());
        }

        throw new EvalException(ctx);
    }

    // ifStatement
    //  : ifStat elseIfStat* elseStat? End
    //  ;
    //
    // ifStat
    //  : If expression Do block
    //  ;
    //
    // elseIfStat
    //  : Else If expression Do block
    //  ;
    //
    // elseStat
    //  : Else Do block
    //  ;
    @Override
    public Value<?> visitIfStatement(SceneLanguageParser.IfStatementContext ctx) {

        // if ...
        if (this.visit(ctx.ifStat().expression()).asBoolean()) {
            return this.visit(ctx.ifStat().block());
        }

        // else if ...
        for (int i = 0; i < ctx.elseIfStat().size(); i++) {
            if (this.visit(ctx.elseIfStat(i).expression()).asBoolean()) {
                return this.visit(ctx.elseIfStat(i).block());
            }
        }

        // else ...
        if (ctx.elseStat() != null) {
            return this.visit(ctx.elseStat().block());
        }

        return Value.NULL;
    }

    // execute a block of statements
    @Override
    public Value<?> visitBlock(BlockContext ctx) {

        //scope = new Scope(scope); // create new local scope
        for (final StatementContext sx : ctx.statement()) {
            this.visit(sx);
        }
        /*
        ExpressionContext ex;
        if ((ex = ctx.expression()) != null) {
            final Value<?> value = this.visit(ex);
            //scope = scope.parent();
            return value;
        }
        //scope = scope.parent();
         */
        return Value.NULL;
    }

    @Override
    public Value<?> visitInclude(IncludeContext ctx) {
        final String quotedFilename = ctx.String().getSymbol().getText();
        final String filename = quotedFilename.substring(1, quotedFilename.length() - 1);
        final FileHandle handle = Gdx.files.internal(filename);
        try (BufferedReader reader = new BufferedReader(handle.reader())) {
            final SceneLanguageLexer lexer = new SceneLanguageLexer(new ANTLRInputStream(reader));
            final SceneLanguageParser parser = new SceneLanguageParser(new CommonTokenStream(lexer));
            parser.setBuildParseTree(true);
            parser.removeErrorListeners();
            parser.addErrorListener(new EvalErrorListener());
            final ParseTree tree = parser.parse();
            visit(tree);
        } catch (final IOException ex) {
            throw new GdxRuntimeException(ex);
        }
        return Value.NULL;
    }

    // forStatement
    // : For Identifier '=' expression To expression OBrace block CBrace
    // ;
    @Override
    public Value<?> visitForStatement(ForStatementContext ctx) {
        final int start = this.visit(ctx.expression(0)).asDouble().intValue();
        final int stop = this.visit(ctx.expression(1)).asDouble().intValue();
        for (int i = start; i <= stop; i++) {
            scope.assign(ctx.Identifier().getText(), Value.of(i));
            final Value<?> returnValue = this.visit(ctx.block());
            if (returnValue != Value.NULL) {
                return returnValue;
            }
        }
        return Value.NULL;
    }

    // whileStatement
    // : While expression OBrace block CBrace
    // ;
    @Override
    public Value<?> visitWhileStatement(WhileStatementContext ctx) {
        while (this.visit(ctx.expression()).asBoolean()) {
            final Value<?> returnValue = this.visit(ctx.block());
            if (returnValue != Value.NULL) {
                return returnValue;
            }
        }
        return Value.NULL;
    }

    @Override
    public Value<?> visitEntity(SceneLanguageParser.EntityContext ctx) {
        final IEntityCommand entity = new EntityVisitor(this).visitEntity(ctx);
        return Value.of(graph.create(entity));
    }

    @Override
    public Value<?> visitBehavior(SceneLanguageParser.BehaviorContext ctx) {
        return Value.of(new BehaviorVisitor(this).visitBehavior(ctx));
    }

    @Override
    public Value<?> visitAttribute(AttributeContext ctx) {
        final String type = ctx.attributeType().getText();
        switch (type) {
            case "Color":
                return resolveColor(ctx);
            case "Position":
                return resolvePosition(ctx);
            case "Vector":
                return resolveVector(ctx);
            case "Rotation":
                return resolveRotation(ctx);
            default:
                LOGGER.error("<visitAttribute> unknown attribute: " + type);
                return Value.NULL;
        }
    }

    @Override
    public Value<?> visitConstantExpression(ConstantExpressionContext ctx) {
        return Value.of(ctx.Constant().getText()); // constants are stored as strings
    }

    private Value<?> resolveVector(AttributeContext ctx) {
        final float x = this.findParameter(ctx, "x", 0).or(Value.of(0f)).asFloat();
        final float y = this.findParameter(ctx, "y", 1).or(Value.of(0f)).asFloat();
        final float z = this.findParameter(ctx, "z", 2).or(Value.of(0f)).asFloat();
        final Vector3 vector = new Vector3(x, y, z);
        return Value.of(vector);
    }

    private Value<?> resolveRotation(AttributeContext ctx) {
        if (ctx.parameter().size() == 2) { // use axis and angle
            final Value<?> axis = this.findParameter(ctx, "axis", 0);
            final Value<?> angle = this.findParameter(ctx, "angle", 1);
            if (axis.isVector() && angle.isFloat()) {
                return Value.of(new Quaternion(axis.asVector(), angle.asFloat()));
            }
        }
        if (ctx.parameter().size() == 3) { // quaternion
            final Value<?> yaw = this.findParameter(ctx, "yaw", 0);
            final Value<?> pitch = this.findParameter(ctx, "pitch", 1);
            final Value<?> roll = this.findParameter(ctx, "roll", 2);
            if (yaw.isFloat() && pitch.isFloat() && roll.isFloat()) {
                return Value.of(new Quaternion().setEulerAngles(yaw.asFloat(), pitch.asFloat(), roll.asFloat()));
            }
        }
        if (ctx.parameter().size() == 4) { // quaternion
            final Value<?> x = this.findParameter(ctx, "x", 0);
            final Value<?> y = this.findParameter(ctx, "y", 1);
            final Value<?> z = this.findParameter(ctx, "z", 2);
            final Value<?> w = this.findParameter(ctx, "w", 3);
            if (x.isFloat() && y.isFloat() && z.isFloat() && w.isFloat()) {
                return Value.of(new Quaternion(x.asFloat(), y.asFloat(), z.asFloat(), w.asFloat()));
            }
        }
        return Value.NULL;
    }

    private Value<?> resolveColor(AttributeContext ctx) {
        final float r = this.findParameter(ctx, "r", 0).or(Value.of(0f)).asFloat();
        final float g = this.findParameter(ctx, "g", 1).or(Value.of(0f)).asFloat();
        final float b = this.findParameter(ctx, "b", 2).or(Value.of(0f)).asFloat();
        final float a = this.findParameter(ctx, "a", 3).or(Value.of(0f)).asFloat();
        final Color color = new Color(r, g, b, a);
        return Value.of(color);
    }

    private Value<?> resolvePosition(AttributeContext ctx) {
        final double x = this.findParameter(ctx, "x", 0).or(Value.of(0d)).asDouble();
        final double y = this.findParameter(ctx, "y", 1).or(Value.of(0d)).asDouble();
        final double z = this.findParameter(ctx, "z", 2).or(Value.of(0d)).asDouble();
        final Position position = new Position(x, y, z);
        return Value.of(position);
    }

    // first try to find the parameter by name, if not found return the parameter at the position
    private Value<?> findParameter(AttributeContext ctx, String identifier, int position) {
        // try to find by identifier
        for (final SceneLanguageParser.ParameterContext parameter : ctx.parameter()) {
            if (parameter.Identifier() != null
                    && identifier.equals(parameter.Identifier().getText())) {
                return this.visit(parameter);
            }
        }
        // try to find by position
        if (ctx.parameter().size() > position
                && ctx.parameter(position).Identifier() == null) {
            return this.visit(ctx.parameter(position));
        }
        return Value.NULL;
    }

}

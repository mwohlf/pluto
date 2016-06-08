package net.wohlfart.pluto.scene.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.wohlfart.pluto.stage.SceneLanguageLexer;
import net.wohlfart.pluto.stage.SceneLanguageParser;

public class MockEvalScope {

    public MockEvalScope() {
        new HeadlessApplication(new MockApplicationListener());
    }

    public Scope invoke(String script) {
        final Scope scope = new Scope();

        try (BufferedReader reader = new BufferedReader(new StringReader(script))) {
            final SceneLanguageLexer lexer = new SceneLanguageLexer(new ANTLRInputStream(reader));

            // pass the tokens to the parser
            final SceneLanguageParser parser = new SceneLanguageParser(new CommonTokenStream(lexer));
            parser.setBuildParseTree(true);
            //parser.setErrorHandler(new BailErrorStrategy());
            final ParseTree tree = parser.parse();

            final Map<String, Function> functions = new HashMap<String, Function>();

            // TODO: do we need the return value from the visit call?
            new EvalVisitor(new MockSceneGraph(), scope, functions).visit(tree);

        } catch (final IOException ex) {
            throw new GdxRuntimeException(ex);
        }

        // TODO Auto-generated method stub
        return scope;
    }

}

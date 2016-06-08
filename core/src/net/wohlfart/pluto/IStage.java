package net.wohlfart.pluto;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

/**
 *
 */
public interface IStage extends ApplicationListener {

    IStage RED_STAGE = new RedStage();

    IStage NULL_STAGE = new NullStage();

    IStage SYSTEM_EXIT = new ExitStage();

    // time needed to finish preparing before this stage is renderable
    float getTasksLeft();

    class ExitStage extends ApplicationAdapter implements IStage {

        @Override
        public float getTasksLeft() {
            return 0;
        }

        @Override
        public void resize(int width, int height) {
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        @Override
        public void render() {
            Logging.ROOT.debug("system exit now");
            Gdx.app.exit();
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }

    }

    class NullStage extends ApplicationAdapter implements IStage {

        @Override
        public float getTasksLeft() {
            return 0;
        }

        @Override
        public void resize(int width, int height) {
        }

        @Override
        public void render() {
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }

    }

    class RedStage extends ApplicationAdapter implements IStage {

        private static final Color CLEAR_COLOR = new Color(1, 0, 0, 1);

        @Override
        public float getTasksLeft() {
            return 0;
        }

        @Override
        public void resize(int width, int height) {
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        @Override
        public void render() {
            Gdx.graphics.getGL20().glClearColor(
                    RedStage.CLEAR_COLOR.r,
                    RedStage.CLEAR_COLOR.g,
                    RedStage.CLEAR_COLOR.b,
                    RedStage.CLEAR_COLOR.a);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }

    }

}

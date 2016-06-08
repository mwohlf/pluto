package net.wohlfart.pluto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;

import net.wohlfart.pluto.resource.ResourceManager;

/**
 * simple stage that displays an image
 */
public class IntroStage extends Stage implements IStage {

    private static final String BACKGROUND_IMAGE = "texture/pluto.jpg";

    protected final IStageManager stageManager;
    protected final ResourceManager resourceManager;

    private Texture background;
    private int x;
    private int y;

    private SpriteBatch batch;

    private final Matrix4 normalProjection = new Matrix4();

    public IntroStage(IStageManager stageManager) {
        this.stageManager = stageManager;
        this.resourceManager = stageManager.getResourceManager();
    }

    @Override
    public void create() {
        // TODO: use data from resourceManager
        background = new Texture(IntroStage.BACKGROUND_IMAGE);
        background.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        batch = new SpriteBatch();
    }

    @Override
    public void render() {
        draw();
    }

    @Override
    public void pause() {
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    @Override
    public void draw() {
        // clear
        Gdx.graphics.getGL20().glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // background
        batch.begin();
        batch.draw(background, 0, 0, -x, -y, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        super.draw();
    }

    @Override
    public void resize(int screenWidth, int screenHeight) {
        batch.setProjectionMatrix(normalProjection.setToOrtho2D(0, 0, screenWidth, screenHeight));
        getViewport().update(screenWidth, screenHeight, true);
        x = (screenWidth - background.getWidth()) / 2;
        y = (screenHeight - background.getHeight()) / 2;
    }

    @Override
    public void resume() {
        // TODO reload resources
    }

    @Override
    public String toString() {
        return IntroStage.class.getSimpleName();
    }

    @Override
    public float getTasksLeft() {
        return 0;
    }

}

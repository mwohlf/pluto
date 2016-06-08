package net.wohlfart.pluto.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import net.wohlfart.pluto.IStage;
import net.wohlfart.pluto.util.Utils;

// see: https://github.com/iXeption/libgdx-transitions/tree/master/src/main/com/ixeption/libgdx/transitions/impl
public class Fade {

    private final IStage stage;

    private final FrameBuffer stageFBO;

    private final Color color = new Color().set(Color.CLEAR);

    private float opacity = 1;

    public Fade(IStage stage) {
        this.stage = stage;
        this.stageFBO = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public void render() {
        assert Utils.isRenderThread();
        if (stage.getTasksLeft() <= 0) {
            stageFBO.begin();
            stage.render();
            stageFBO.end();
        }
    }

    public IStage getStage() {
        return stage;
    }

    public void render(Batch batch) {
        final Texture texture = stageFBO.getColorBufferTexture();
        final float width = texture.getWidth();
        final float height = texture.getHeight();

        color.set(Color.WHITE);
        color.a = opacity;
        batch.setColor(color);
        batch.draw(texture, 0, 0, width / 2, height / 2, width, height, 1, 1, 0, 0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);

    }

    @Override
    public String toString() {
        return "Fade"
                + " stage: " + stage;
    }

}

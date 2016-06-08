package net.wohlfart.pluto.gui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GuiViewport extends Viewport implements DensityViewport {
    private float unitsPerPixel = 0.75f;

    /** Creates a new viewport using a new {@link OrthographicCamera}. */
    public GuiViewport() {
        this(new OrthographicCamera());
    }

    private GuiViewport(Camera camera) {
        super();
        setCamera(camera);
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        setScreenBounds(0, 0, screenWidth, screenHeight);
        // final float aspectRatio = (float) screenWidth / (float) screenHeight;
        setWorldSize(screenWidth * unitsPerPixel, screenHeight * unitsPerPixel);
        apply(centerCamera);
    }

    @Override
    public void updateDensity(float density) {
        unitsPerPixel = (1f / density) * 0.75f;
        update(getScreenWidth(), getScreenHeight(), true);
    }

}

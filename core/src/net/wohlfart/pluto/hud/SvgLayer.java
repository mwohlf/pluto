package net.wohlfart.pluto.hud;


import com.badlogic.gdx.Gdx;
import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.Version.VERSION_MAJOR;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL2.*;


public class SvgLayer implements HudLayer {

    final long ctx;
    private final NVGColor colorA = NVGColor.create().r(1).g(0).b(0).a(0.6f);

    public SvgLayer() {
        System.out.println("VERSION_MAJOR: " + VERSION_MAJOR );
        ctx = nvgCreateGL2(NVG_ANTIALIAS | NVG_STENCIL_STROKES | NVG_DEBUG);
    }

    @Override
    public void render() {
        final int width = Gdx.graphics.getWidth();
        final int height = Gdx.graphics.getHeight();

        nvgBeginFrame(ctx, width, height, 1.0f);
        nvgSave(ctx);

        nvgBeginPath(ctx);
        nvgRoundedRect(ctx, 30, 30, 340, 30, 5);
        nvgFillColor(ctx, colorA);
        nvgFill(ctx);

        nvgRestore(ctx);
        nvgEndFrame(ctx);

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

}

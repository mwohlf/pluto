package net.wohlfart.pluto.hud;

import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgRestore;
import static org.lwjgl.nanovg.NanoVG.nvgRoundedRect;
import static org.lwjgl.nanovg.NanoVG.nvgSave;
import static org.lwjgl.nanovg.NanoVGGL2.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL2.NVG_DEBUG;
import static org.lwjgl.nanovg.NanoVGGL2.NVG_STENCIL_STROKES;
import static org.lwjgl.nanovg.NanoVGGL2.nvgCreateGL2;

import org.lwjgl.nanovg.NVGColor;

import com.badlogic.gdx.Gdx;

public class SvgLayer implements HudLayer {

    final long ctx;
    private final NVGColor colorA = NVGColor.create().r(1).g(0).b(0).a(0.6f);

    public SvgLayer() {
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

package net.wohlfart.pluto.hud;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.Camera;
import net.wohlfart.pluto.controller.Command;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.entity.PickSystem;
import net.wohlfart.pluto.resource.FontManager;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.IsPickable;

public class TextLayer extends SpriteBatch implements HudLayer {
    public static final String INFO_ACTION = "info";
    public static final String PROFILER_ACTION = "profiler";

    private static final int INFO_TOP_LEFT_X = 10;
    private static final int INFO_TOP_LEFT_Y = 10;

    protected final ResourceManager resourceManager;

    private final Matrix4 normalProjection = new Matrix4();

    private final ToggleInfoCommand toggleInfoCommand;

    private final GlyphLayout info;
    private final StringBuilder infoText;

    private final Camera cam;
    private final PickSystem pickSystem;

    private BitmapFont bitmapFont;

    private Vector3 tmpVector = new Vector3();

    TextLayer(ISceneGraph graph, ResourceManager resourceManager) {
        this.cam = graph.getCamera();
        this.pickSystem = graph.getPickSystem();
        this.resourceManager = resourceManager;
        info = new GlyphLayout();
        infoText = new StringBuilder();
        bitmapFont = resourceManager.getFont(FontManager.FontKey.TEXT_FONT);
        bitmapFont.setColor(1.0f, 1.0f, 1.0f, 0.7f);
        toggleInfoCommand = new ToggleInfoCommand();
        resourceManager.getCommandMap().put(toggleInfoCommand.getKey(), toggleInfoCommand);
    }

    @Override
    public void render() {
        if (resourceManager.getInfoEnabled()) {
            clearInfo();
            appendStatistics();
            appendProfiling();
            appendPickData();
            displayInfo();
        }
    }

    private void appendProfiling() {
        infoText.append("calls: ").append(GLProfiler.calls).append("\n");
        infoText.append("drawCalls: ").append(GLProfiler.drawCalls).append("\n");
        infoText.append("shaderSwitches: ").append(GLProfiler.shaderSwitches).append("\n");
        infoText.append("textureBindings: ").append(GLProfiler.textureBindings).append("\n");
        infoText.append("vertexCount.average: ").append(GLProfiler.vertexCount.average).append("\n");
        infoText.append("vertexCount.count: ").append(GLProfiler.vertexCount.count).append("\n");
        infoText.append("vertexCount.latest: ").append(GLProfiler.vertexCount.latest).append("\n");
        infoText.append("vertexCount.max: ").append(GLProfiler.vertexCount.max).append("\n");
        infoText.append("vertexCount.min: ").append(GLProfiler.vertexCount.min).append("\n");
        infoText.append("vertexCount.total: ").append(GLProfiler.vertexCount.total).append("\n");
        infoText.append("vertexCount.value: ").append(GLProfiler.vertexCount.value).append("\n");
        GLProfiler.reset();
    }

    private void appendPickData() {
        final Entity entity = pickSystem.getCurrentPick();
        if (entity != EntityPool.NULL) {
            tmpVector = entity.getComponent(HasPosition.class).getPosition().get(tmpVector);
            infoText.append("pick position:    ").append("[")
                    .append(round10(tmpVector.x)).append(",")
                    .append(round10(tmpVector.y)).append(",")
                    .append(round10(tmpVector.z)).append("]")
                    .append("\n");
            tmpVector = cam.project(tmpVector);
            infoText.append("screen coordinates:    ").append("[")
                    .append(round10(tmpVector.x)).append(",")
                    .append(round10(tmpVector.y)).append("]")
                    .append("\n");
            final IsPickable pickable = entity.getComponent(IsPickable.class);
            infoText.append("pickable.range:   ").append(pickable.getPickRange()).append("\n");
            infoText.append("pickable.transform:").append("\n");
            append(infoText, pickable.getTransform());
            infoText.append("\n");
        }
    }

    private void append(StringBuilder sb, Matrix4 m) {
        final float[] val = m.val;
        sb.append("[")
                .append(round100(val[Matrix4.M00])).append("|")
                .append(round100(val[Matrix4.M01])).append("|")
                .append(round100(val[Matrix4.M02])).append("|")
                .append(round100(val[Matrix4.M03])).append("\n");
        sb.append(" ")
                .append(round100(val[Matrix4.M10])).append("|")
                .append(round100(val[Matrix4.M11])).append("|")
                .append(round100(val[Matrix4.M12])).append("|")
                .append(round100(val[Matrix4.M13])).append("\n");
        sb.append(" ")
                .append(round100(val[Matrix4.M20])).append("|")
                .append(round100(val[Matrix4.M21])).append("|")
                .append(round100(val[Matrix4.M22])).append("|")
                .append(round100(val[Matrix4.M23])).append("\n");
        sb.append(" ")
                .append(round100(val[Matrix4.M30])).append("|")
                .append(round100(val[Matrix4.M31])).append("|")
                .append(round100(val[Matrix4.M32])).append("|")
                .append(round100(val[Matrix4.M33])).append("]\n");
    }

    private void appendStatistics() {
        final Graphics g = Gdx.graphics;
        infoText.append("fps:    ").append(g.getFramesPerSecond()).append("\n");
        infoText.append("frames: ").append(g.getFrameId()).append("\n");
        infoText.append("continuous: ").append(g.isContinuousRendering()).append("\n");
        infoText.append("density [dpi]: ").append(g.getDensity() * 160f).append("\n");
        final int w = g.getWidth();
        final int h = g.getHeight();
        infoText.append("display [px]: ").append(w).append("x").append(h).append("\n");
        final float x = round10(w / g.getPpcX());
        final float y = round10(h / g.getPpcY());
        infoText.append("display [cm]: ").append(x).append("x").append(y).append("\n");

    }

    private float round10(float n) {
        return Math.round(10f * n) / 10f;
    }

    private float round100(float n) {
        return Math.round(100f * n) / 100f;
    }

    private void clearInfo() {
        infoText.setLength(0);
    }

    private void displayInfo() {
        normalProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        setProjectionMatrix(normalProjection);
        info.setText(bitmapFont, infoText);
        begin();
        bitmapFont.draw(this, info, TextLayer.INFO_TOP_LEFT_X, Gdx.graphics.getHeight() - TextLayer.INFO_TOP_LEFT_Y);
        end();
    }

    @Override
    public void dispose() {
        super.dispose();
        bitmapFont.dispose();
        resourceManager.getCommandMap().remove(toggleInfoCommand.getKey());
    }

    @Override
    public void resume() {
        bitmapFont = resourceManager.getFont(FontManager.FontKey.TEXT_FONT);
        bitmapFont.setColor(1.0f, 1.0f, 1.0f, 0.7f);
    }

    private class ToggleInfoCommand implements Command {

        @Override
        public String getKey() {
            return TextLayer.INFO_ACTION;
        }

        @Override
        public void execute() {
            final boolean show = !resourceManager.getInfoEnabled();
            if (show) {
                GLProfiler.enable();
            } else {
                GLProfiler.disable();
            }
            TextLayer.this.resourceManager.setInfoEnabled(show);
        }

    }

}

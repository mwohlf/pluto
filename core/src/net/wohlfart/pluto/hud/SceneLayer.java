package net.wohlfart.pluto.hud;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import net.wohlfart.pluto.Camera;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.entity.PickSystem;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.scene.properties.HasPosition;

class SceneLayer extends ShapeRenderer implements HudLayer {

    private static final float W = 7;

    private final Matrix4 normalProjection = new Matrix4();

    private final Position tmpPosition = new Position();
    private final Vector2 tmpVector2 = new Vector2();

    private final Camera cam;

    private final PickSystem pickSystem;

    SceneLayer(ISceneGraph graph) {
        this.cam = graph.getCamera();
        this.pickSystem = graph.getPickSystem();
    }

    @Override
    public void render() {
        final Entity focused = pickSystem.getCurrentPick();
        if (focused != EntityPool.NULL) {
            focused.getComponent(HasPosition.class).getPosition().get(tmpPosition);
            if (tmpPosition.z < 0) { // target must be in front
                cam.project(tmpPosition);
                tmpVector2.set((float) tmpPosition.x, (float) tmpPosition.y);
                showFocused(tmpVector2);
            }
        }
    }

    private void showFocused(Vector2 p) {
        normalProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        setProjectionMatrix(normalProjection);
        begin(ShapeType.Line);
        setColor(0.8f, 0.4f, 0.5f, 0.7f);
        //circle(p.x, p.y, W);
        polygon(new float[] {
                p.x, p.y + SceneLayer.W,
                p.x - SceneLayer.W, p.y,
                p.x, p.y - SceneLayer.W,
                p.x + SceneLayer.W, p.y
        });
        end();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void resume() {
        // nothing to do here
    }

}

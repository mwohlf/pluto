package net.wohlfart.pluto.scene.properties;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class HasRenderables implements Component, Poolable, RenderableProvider {
    private RenderableProvider delegate;

    @Override
    public void reset() {
        delegate = NULL;
    }

    public HasRenderables withDelegate(RenderableProvider delegate) {
        this.delegate = delegate;
        return this;
    }

    @Override
    public void getRenderables(Array<Renderable> renderables,
            Pool<Renderable> pool) {
        delegate.getRenderables(renderables, pool);
    }

    private static final RenderableProvider NULL = (renderables, pool) -> {
        // do nothing
    };

}

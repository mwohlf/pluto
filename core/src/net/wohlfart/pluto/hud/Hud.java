package net.wohlfart.pluto.hud;

import java.util.ArrayList;
import java.util.Collection;

import net.wohlfart.pluto.event.EventBus;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.ISceneGraph;

public class Hud {

    private final Collection<HudLayer> layers = new ArrayList<>();

    public Hud(ISceneGraph graph, EventBus eventBus, ResourceManager resourceManager) {
        layers.add(new TextLayer(graph, resourceManager));
        layers.add(new SceneLayer(graph));
    }

    public void render() {
        for (final HudLayer layer : layers) {
            layer.render();
        }
    }

    public void dispose() {
        for (final HudLayer layer : layers) {
            layer.dispose();
        }
    }

}

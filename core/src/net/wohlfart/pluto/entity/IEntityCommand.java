package net.wohlfart.pluto.entity;

import com.badlogic.ashley.core.Entity;

import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;

/**
 * command to create entities in the graph
 */
public interface IEntityCommand {

    long NULL_UID = 0;

    long getUid();

    /**
     * this method is run right away on the render thread, only used for cam
     * no callback needed since this method shouldn't return early
     */
    void runNow(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity);

    /**
     * this method is run off the render thread, override if anything compute intensive
     * needs to be done in the factory, however there is no access to the GL context
     * also this method is not called when registering an entity immediately in the scene-graph
     * no callback used since the method runs off the render thread after it finished the call to
     * registerSync is made on the render thread
     */
    void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity);

    /**
     * this method is run on the render thread after the async call finished
     * this method might use a callback
     */
    void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity);

    Entity NULL_ENTITY = new Entity();

}

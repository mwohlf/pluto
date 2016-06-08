package net.wohlfart.pluto.scene;

import net.wohlfart.pluto.entity.EntityPool;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.collision.Ray;

public interface IPickFacade {

    Ray NULL_RAY = new Ray();

    IPickFacade NULL = new IPickFacade() {

        @Override
        public Ray getPickRay(float x, float y) {
            return NULL_RAY;
        }

        @Override
        public Entity getPickedEntity(Ray pickRay) {
            return EntityPool.NULL;
        }

        @Override
        public void selectEntity(Entity entity) {
            // do nothing
        }

    };

    Ray getPickRay(float x, float y);

    Entity getPickedEntity(Ray pickRay);

    void selectEntity(Entity entity);

}

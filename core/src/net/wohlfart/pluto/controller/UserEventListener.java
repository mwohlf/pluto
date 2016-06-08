package net.wohlfart.pluto.controller;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.collision.Ray;

import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.scene.IPickFacade;

public interface UserEventListener {

    void pick(Ray pickRay);

    void rightMenu();

    UserEventListener NULL = new UserEventListener() {

        @Override
        public void pick(Ray pickRay) {
            // do nothing
        }

        @Override
        public void rightMenu() {
            // do nothing
        }

    };

    class PickRayListener implements UserEventListener {
        final IPickFacade facade;

        public PickRayListener(IPickFacade facade) {
            this.facade = facade;
        }

        @Override
        public void pick(Ray pickRay) {
            final Entity entity = facade.getPickedEntity(pickRay);
            if (entity != EntityPool.NULL) {
                facade.selectEntity(entity);
            }
        }

        @Override
        public void rightMenu() {
            // TODO Auto-generated method stub

        }
    }

}

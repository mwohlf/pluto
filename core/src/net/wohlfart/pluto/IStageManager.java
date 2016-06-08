package net.wohlfart.pluto;

import javax.annotation.Nonnull;

import net.wohlfart.pluto.resource.ResourceManager;

public interface IStageManager {

    void scheduleTransitionToStage(@Nonnull IStageTransition transition, @Nonnull IStage newStage);

    @Nonnull
            ResourceManager getResourceManager();

    @Nonnull
            IStage getCurrentStage();

}

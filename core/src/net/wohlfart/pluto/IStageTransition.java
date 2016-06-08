package net.wohlfart.pluto;

import javax.annotation.Nonnull;

/**
 * render the old stage till the new one is ready plus any transition effects
 */
public interface IStageTransition extends IStage {

    IStageTransition NULL_TRANSITION = new NullTransition();

    IStageTransition initTransition(IStage oldStage, IStage newStage);

    class NullTransition implements IStageTransition {

        @Override
        public float getTasksLeft() {
            throw new IllegalAccessError("<getTimeLeft> shouldn't be called");
        }

        @Override
        public void create() {
            throw new IllegalAccessError("<getTimeLeft> shouldn't be called");
        }

        @Override
        public void resize(int width, int height) {
            throw new IllegalAccessError("<getTimeLeft> shouldn't be called");
        }

        @Override
        public void render() {
            throw new IllegalAccessError("<getTimeLeft> shouldn't be called");
        }

        @Override
        public void pause() {
            throw new IllegalAccessError("<getTimeLeft> shouldn't be called");
        }

        @Override
        public void resume() {
            throw new IllegalAccessError("<getTimeLeft> shouldn't be called");
        }

        @Override
        public void dispose() {
            throw new IllegalAccessError("<getTimeLeft> shouldn't be called");
        }

        @Override
        public IStageTransition initTransition(IStage oldStage, IStage newStage) {
            throw new IllegalAccessError("<getTimeLeft> shouldn't be called");
        }

    }

    // render the old stage until the new is ready
    class SwitchTransition implements IStageTransition {

        @Nonnull
        IStage oldStage = IStage.RED_STAGE;

        @Nonnull
        IStage newStage = IStage.RED_STAGE;

        @Override
        public IStageTransition initTransition(@Nonnull IStage oldStage, @Nonnull IStage newStage) {
            this.oldStage = oldStage;
            this.newStage = newStage;
            return this;
        }

        @Override
        public void dispose() {
            oldStage.pause();
            oldStage.dispose();
            oldStage = IStage.RED_STAGE;
        }

        @Override
        public float getTasksLeft() {
            return newStage.getTasksLeft();
        }

        @Override
        public void create() {
            throw new IllegalAccessError("<create> shouldn't be called");
        }

        @Override
        public void resize(int width, int height) {
            oldStage.resize(width, height);
        }

        @Override
        public void render() {
            oldStage.render();
        }

        @Override
        public void pause() {
            oldStage.pause();
        }

        @Override
        public void resume() {
            oldStage.resume();
        }

        @Override
        public String toString() {
            return "SwitchTransition ["
                    + "oldStage=" + oldStage + ", "
                    + "newStage=" + newStage
                    + "]";
        }

    }

}

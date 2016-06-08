package net.wohlfart.pluto.gui;

import java.util.Random;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.Viewport;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import net.wohlfart.pluto.resource.ActorAccessor;

enum Layout implements ILayout {
    CENTER {
        private static final int TOP_OFFSET = 20;

        @Override
        public Timeline show(IWidgetContainer container) {
            final Viewport viewport = container.getStage().getViewport();
            float yPos = viewport.getWorldHeight() - TOP_OFFSET;
            final Timeline parallel = Timeline.createParallel();
            for (final Actor actor : container.getChildren()) {
                final Timeline sequence = Timeline.createSequence();
                sequence.push(Tween.to(actor, ActorAccessor.POSITION, 0)
                        .target(offscreenX(viewport, actor),
                                offscreenY(viewport, actor)));
                sequence.push(Tween.to(actor, ActorAccessor.VISIBLE, 0).target(
                        ActorAccessor.IS_VISIBLE_VALUE));
                sequence.push(Tween
                        .to(actor, ActorAccessor.POSITION, 1.5f)
                        .delay(Layout.RANDOM.nextFloat())
                        .target(centerX(viewport.getWorldWidth(),
                                actor.getWidth()),
                                yPos -= actor.getHeight() + 5));
                parallel.push(sequence);
            }
            return parallel;
        }

        @Override
        public Timeline hide(IWidgetContainer container) {
            final Viewport viewport = container.getStage().getViewport();
            final Timeline parallel = Timeline.createParallel();
            for (final Actor actor : container.getChildren()) {
                final Timeline sequence = Timeline.createSequence();
                sequence.push(Tween
                        .to(actor, ActorAccessor.POSITION, 1.5f)
                        .delay(Layout.RANDOM.nextFloat())
                        .target(offscreenX(viewport, actor),
                                offscreenY(viewport, actor)));
                sequence.push(Tween.to(actor, ActorAccessor.VISIBLE, 0).target(
                        ActorAccessor.IS_NOT_VISIBLE_VALUE));
                parallel.push(sequence);
            }
            return parallel;
        }

        @Override
        public Timeline refresh(IWidgetContainer container) {
            final Viewport viewport = container.getStage().getViewport();
            float yPos = viewport.getWorldHeight() - TOP_OFFSET;
            final Timeline sequence = Timeline.createSequence();
            for (final Actor actor : container.getChildren()) {
                sequence.push(Tween.to(actor, ActorAccessor.POSITION, 0)
                        .target(centerX(viewport.getWorldWidth(),
                                actor.getWidth()),
                                yPos -= actor.getHeight() + 5));
            }
            return sequence;
        }
    },

    SOUTH {
        @Override
        public Timeline show(IWidgetContainer container) {
            float yPos = 0;
            final Timeline parallel = Timeline.createParallel();
            for (final Actor actor : container.getChildren()) {
                final Timeline sequence = Timeline.createSequence();
                sequence.push(Tween.to(actor, ActorAccessor.POSITION, 0)
                        .target(5, -actor.getHeight()));
                sequence.push(Tween.to(actor, ActorAccessor.VISIBLE, 0).target(
                        ActorAccessor.IS_VISIBLE_VALUE));
                sequence.push(Tween
                        .to(actor, ActorAccessor.POSITION, 1.5f)
                        .delay(Layout.RANDOM.nextFloat())
                        .target(5, (yPos += actor.getHeight() + 5) - actor.getHeight()));
                parallel.push(sequence);
            }
            return parallel;
        }

        @Override
        public Timeline hide(IWidgetContainer container) {
            final Timeline parallel = Timeline.createParallel();
            for (final Actor actor : container.getChildren()) {
                final Timeline sequence = Timeline.createSequence();
                sequence.push(Tween
                        .to(actor, ActorAccessor.POSITION, 1.5f)
                        .delay(Layout.RANDOM.nextFloat())
                        .target(5, -actor.getHeight()));
                sequence.push(Tween.to(actor, ActorAccessor.VISIBLE, 0)
                        .target(ActorAccessor.IS_NOT_VISIBLE_VALUE));
                parallel.push(sequence);
            }
            return parallel;
        }

        @Override
        public Timeline refresh(IWidgetContainer container) {
            float yPos = 0;
            final Timeline sequence = Timeline.createSequence();
            for (final Actor actor : container.getChildren()) {
                sequence.push(Tween.to(actor, ActorAccessor.POSITION, 0)
                        .target(5, (yPos += actor.getHeight() + 5) - actor.getHeight()));
            }
            return sequence;
        }
    }

    ;

    private static final Random RANDOM = new Random();

    float offscreenX(Viewport viewport, Actor button) {
        if (Layout.RANDOM.nextInt(2) == 0) {
            return -button.getWidth();
        } else {
            return viewport.getWorldWidth();
        }
    }

    float offscreenY(Viewport viewport, Actor button) {
        if (Layout.RANDOM.nextInt(2) == 0) {
            return -button.getHeight();
        } else {
            return viewport.getWorldHeight();
        }
    }

    float centerX(float containerWidth, float width) {
        return (containerWidth - width) / 2f;
    }

}

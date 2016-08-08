package net.wohlfart.pluto.scene.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.stage.SceneLanguageParser;
import net.wohlfart.pluto.stage.SceneLanguageParser.ParameterContext;

public class BehaviorVisitor extends ParameterApplyVisitor<IBehavior> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BehaviorVisitor.class);

    private static final BehaviorFactoryDecorators DECORATORS = new BehaviorFactoryDecorators();

    public BehaviorVisitor(EvalVisitor visitor) {
        super(visitor);
    }

    @Override
    public IBehavior visitBehavior(SceneLanguageParser.BehaviorContext ctx) {
        final String type = ctx.behaviorType().getText();
        LOGGER.info("<visitBehavior> type: {0}", type);
        final IFactoryDecorator<IBehavior> factory = DECORATORS.get(type);
        final IBehavior entity = factory.create();
        // test fail if we use lambda here
        for (final ParameterContext propertyContext : ctx.parameter()) {
            withFactory(factory).withEntity(entity).visitParameter(propertyContext);
        }
        return entity;
    }

}

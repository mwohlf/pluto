package net.wohlfart.pluto.scene.lang;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.stage.SceneLanguageParser;
import net.wohlfart.pluto.stage.SceneLanguageParser.ParameterContext;

public class BehaviorVisitor extends ParameterApplyVisitor<IBehavior> {

    private static final Logger LOGGER = LoggerService.forClass(BehaviorVisitor.class);

    private static final BehaviorFactoryDecorators DECORATORS = new BehaviorFactoryDecorators();

    public BehaviorVisitor(EvalVisitor visitor) {
        super(visitor);
    }

    @Override
    public IBehavior visitBehavior(SceneLanguageParser.BehaviorContext ctx) {
        final String type = ctx.behaviorType().getText();
        LOGGER.info("<visitBehavior> type: " + type);
        this.factory = DECORATORS.get(type);
        this.entity = factory.create();
        // test fail if we use lambda here
        for (final ParameterContext propertyContext : ctx.parameter()) {
            visitParameter(propertyContext);
        }
        return entity;
    }

}

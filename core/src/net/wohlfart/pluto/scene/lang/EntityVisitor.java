package net.wohlfart.pluto.scene.lang;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.stage.SceneLanguageParser;
import net.wohlfart.pluto.stage.SceneLanguageParser.ParameterContext;

// see: http://jakubdziworski.github.io/java/2016/04/01/antlr_visitor_vs_listener.html
// a new visitor for each entity
public class EntityVisitor extends ParameterApplyVisitor<IEntityCommand> {

    private static final Logger LOGGER = LoggerService.forClass(EntityVisitor.class);

    private static final EntityFactoryDecorators DECORATORS = new EntityFactoryDecorators();

    public EntityVisitor(EvalVisitor visitor) {
        super(visitor);
    }

    @Override
    public IEntityCommand visitEntity(SceneLanguageParser.EntityContext ctx) {
        final String type = ctx.entityType().getText();
        LOGGER.info("<visitEntity> type: " + type);
        final IFactoryDecorator<IEntityCommand> factory = DECORATORS.get(type);
        final IEntityCommand entity = factory.create();
        for (final ParameterContext propertyContext : ctx.parameter()) {
            withFactory(factory).withEntity(entity).visitParameter(propertyContext);
        }
        return entity;
    }

}

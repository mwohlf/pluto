package net.wohlfart.pluto.scene.lang;

import java.util.Map;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.stage.SceneLanguageParser;
import net.wohlfart.pluto.stage.SceneLanguageParser.ParameterContext;

// see: http://jakubdziworski.github.io/java/2016/04/01/antlr_visitor_vs_listener.html
// a new visitor for each entity
public class EntityVisitor extends ElementVisitor<IEntityCommand> {

    private static final Logger LOGGER = LoggerService.forClass(EntityVisitor.class);

    private static final EntityFactoryDecorators DECORATORS = new EntityFactoryDecorators();

    public EntityVisitor(ISceneGraph graph, Scope scope, Map<String, Function> functions) {
        super(graph, scope, functions);
    }

    @Override
    public IEntityCommand visitEntity(SceneLanguageParser.EntityContext ctx) {
        final String type = ctx.entityType().getText();
        LOGGER.info("<visitEntity> type: " + type);
        this.factory = DECORATORS.get(type);
        this.entity = factory.create();
        for (final ParameterContext propertyContext : ctx.parameter()) {
            visitParameter(propertyContext);
        }
        return entity;
    }

}

package net.wohlfart.pluto.stage.loader;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;
import com.google.common.util.concurrent.ListenableFuture;

import net.wohlfart.pluto.Logging;
import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.ai.btree.Parallel;
import net.wohlfart.pluto.ai.btree.Sequential;
import net.wohlfart.pluto.entity.Callback;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.scene.lang.IFactoryDecorator;
import net.wohlfart.pluto.util.Utils;

public class JsonConverter {

    protected static final Logger LOGGER = LoggerService.forClass(JsonConverter.class);

    public static final String WAYPOINT_TAG = "waypoint";

    public static final String BEHAVIOR_TAG = "behavior";

    public static final String TYPE_TAG = "type";

    public static final String UID_TAG = "uid";
    private static final Pattern DIGIT_CHARS = Pattern.compile("\\d+");

    /**
     * creates and submits graph entities
     * - uid is the json name
     * - the class is derived from the json value for type
     *
     * @throws Exception
     */
    public void runEntityCommand(ISceneGraph graph, JsonValue jsonValue, final Callback<Entity> entityCallback) throws Exception {
        assert Utils.isRenderThread() : "<runEntityCommand> running on wrong thread";

        final IFactoryDecorator<IEntityCommand> factory = pickFactory(jsonValue);
        if (factory == null) {
            JsonConverter.LOGGER.error("<runEntityCommand> no factory for jsonValue: '" + jsonValue + "', ignoring");
            return;
        }

        final IEntityCommand delegate = createCommand(jsonValue, factory);
        // runs async, returns early, callback when all properties are set
        setProperties(graph, jsonValue, factory, delegate, entityCommand -> {
            // running the  command
            JsonConverter.LOGGER.debug("<runEntityCommand> submitting command: " + entityCommand);
            final ListenableFuture<Entity> futureEntity = graph.create(entityCommand);
            futureEntity.addListener(new Runnable() {
                @Override
                public void run() {
                    assert Utils.isRenderThread();
                    try {
                        JsonConverter.LOGGER.debug("<runEntityCommand> entity ready: " + futureEntity.get() + " delegate was " + entityCommand);
                        entityCallback.ready(futureEntity.get());
                    } catch (InterruptedException | ExecutionException ex) {
                        Logging.ROOT.error(ex, "error while running callback");
                    }
                }
            }, graph); //MoreExecutors.directExecutor());
        });
    }

    // runs sync: pick the correct factory, either by the type attribute or by the json name value
    private IFactoryDecorator<IEntityCommand> pickFactory(JsonValue jsonValue) {
        IFactoryDecorator<IEntityCommand> result = null;
        // try to use the type tag to find the factory for the command
        if (jsonValue.has(JsonConverter.TYPE_TAG)) {
            result = FactoryContainer.getEntityCommand(jsonValue.getString(JsonConverter.TYPE_TAG));
        }
        // fallback to the json name
        if (result == null) {
            result = FactoryContainer.getEntityCommand(jsonValue.name);
        }
        return result;
    }

    // runs sync: create the command and set the uid
    private IEntityCommand createCommand(JsonValue jsonValue, IFactoryDecorator<IEntityCommand> factory) throws Exception {
        final IEntityCommand delegate = factory.create();
        JsonConverter.LOGGER.debug("<createCommand> created delegate " + delegate);
        final Method setterMethod = factory.getMethodFor(JsonConverter.UID_TAG);
        if (setterMethod != null
                && jsonValue.name != null
                && DIGIT_CHARS.matcher(jsonValue.name).matches()) {
            JsonConverter.LOGGER.debug("<createCommand> calling setter method: " + setterMethod.getName() + "(...) with jsonValue.name '" + jsonValue.name + "' ");
            setterMethod.invoke(delegate, Long.parseLong(jsonValue.name));
        } else {
            if (!JsonConverter.WAYPOINT_TAG.equals(jsonValue.name)) { // waypoint is ok without uid
                JsonConverter.LOGGER.error("<createCommand> no setter found for uid or name value not an integer value for '" + jsonValue.name + "' ");
            }
        }
        return delegate;
    }

    // set the values for the command, might return async if one of the properties is a entity
    private void setProperties(ISceneGraph graph, JsonValue jsonValue, IFactoryDecorator<?> factory, IEntityCommand delegate, Callback<IEntityCommand> callback) throws Exception {
        // setting the properties
        final AtomicInteger openCalls = new AtomicInteger(jsonValue.size);
        for (JsonValue childJson = jsonValue.child; childJson != null; childJson = childJson.next) {
            if (JsonConverter.TYPE_TAG.equals(childJson.name)) {
                decrementToCallback(openCalls, delegate, callback);
                continue; // skipping type tag since it is already used to select the factory
            }
            final Method setterMethod = factory.getMethodFor(childJson.name);
            if (setterMethod == null) {
                JsonConverter.LOGGER.error("<setProperties> unknown setter for json value '" + childJson + "'");
                decrementToCallback(openCalls, delegate, callback);
                continue;
            }
            JsonConverter.LOGGER.debug("<setProperties> calling setter method: " + setterMethod.getName() + "(...) with childJson.name: '" + childJson.name + "' ");
            setSingleProperty(graph, childJson, setterMethod, delegate, object -> decrementToCallback(openCalls, delegate, callback));
        }
    }

    /**
     * setting a single parameters for an entity
     */
    private void setSingleProperty(ISceneGraph graph, JsonValue jsonValue, Method method, Object target, Callback<Object> callback) throws Exception {

        // try to find by converter name e.g. int parameters might be created from specific constants
        final String converterName = method.getAnnotation(EntityProperty.class).type();
        if (ConverterContainer.containsKey(converterName)) {
            JsonConverter.LOGGER.debug("<setEntityProperty> invoking for converterName: " + converterName + " jsonValue.name: " + jsonValue.name);
            method.invoke(target, ConverterContainer.get(converterName).convert(jsonValue));
            callback.ready(target);
            return;
        }

        // try to find setter by parameter type
        final Class<?> paramType = method.getParameterTypes()[0];
        if (ConverterContainer.containsKey(paramType)) {
            JsonConverter.LOGGER.debug("<setEntityProperty> invoking for paramType: " + paramType.getSimpleName() + " jsonValue.name: " + jsonValue.name);
            method.invoke(target, ConverterContainer.get(paramType).convert(jsonValue));
            callback.ready(target);
            return;
        }

        // special treatment, try to resolve behaviors
        if (JsonConverter.BEHAVIOR_TAG.equals(jsonValue.name)) {
            JsonConverter.LOGGER.debug("<setEntityProperty> creating behavior for type" + paramType.getSimpleName() + " jsonValue.name: " + jsonValue.name);
            setBehaviorProperty(graph, jsonValue, method, target, callback);
            return;
        }

        JsonConverter.LOGGER.error("<setEntityProperty> didn't find property converter jsonValue.name: " + jsonValue.name);
    }

    private void setBehaviorProperty(ISceneGraph graph, JsonValue jsonValue, Method method, Object target, Callback<Object> callback) throws Exception {
        if (jsonValue.size == 1) {
            JsonConverter.LOGGER.debug("<setBehaviorProperty> single behavior");
            convertBehavior(graph, jsonValue.child, behavior -> {
                try {
                    method.invoke(target, behavior);
                    callback.ready(target);
                } catch (final Throwable ex) {
                    throw new GdxRuntimeException(ex);
                }
            });
        } else { // size > 1
            final IBehavior behavior;
            final Deque<JsonValue> elements;
            if (jsonValue.isArray()) {
                JsonConverter.LOGGER.debug("<setBehaviorProperty> sequential behavior");
                // sequential behavior, the array elements are wrapped
                behavior = new Sequential();
                elements = new ArrayDeque<>();
                for (JsonValue entry = jsonValue.child; entry != null; entry = entry.next) {
                    elements.add(entry.child);
                }
            } else { // !jsonValue.isArray()
                JsonConverter.LOGGER.debug("<setBehaviorProperty> parallel behavior");
                // parallel behavior
                behavior = new Parallel();
                elements = new ArrayDeque<>();
                for (JsonValue entry = jsonValue.child; entry != null; entry = entry.next) {
                    elements.add(entry);
                }
            }
            collectBehaviors(
                    graph,
                    behavior,
                    elements,
                    () -> {
                        try {
                            method.invoke(target, behavior);
                            callback.ready(target);
                        } catch (final Exception ex) {
                            throw new GdxRuntimeException(ex);
                        }
                    });
        }
    }

    private void collectBehaviors(ISceneGraph graph, IBehavior parent, Deque<JsonValue> elements, Runnable runnable) throws Exception {
        if (elements.isEmpty()) {
            runnable.run();
        } else {
            final JsonValue elem = elements.pop();
            convertBehavior(graph, elem, behavior -> {
                try {
                    parent.addChild(behavior);
                    collectBehaviors(graph, parent, elements, runnable);
                } catch (final Throwable ex) {
                    throw new GdxRuntimeException(ex);
                }
            });
        }
    }

    private void convertBehavior(ISceneGraph graph, JsonValue jsonValue, Callback<IBehavior> callback) throws Exception {
        assert !JsonConverter.BEHAVIOR_TAG.equals(jsonValue.name); // not a meta behavior
        JsonConverter.LOGGER.debug("<convertBehavior> called with size: " + jsonValue.size + " name: " + jsonValue.name);
        final IFactoryDecorator<IBehavior> behaviorDecorator = FactoryContainer.getBehavior(jsonValue.name);
        if (behaviorDecorator == null) {
            JsonConverter.LOGGER.error("<convertBehavior> no behavior for json name '" + jsonValue.name + "', ignoring");
            return;
        }
        final IBehavior delegate = behaviorDecorator.create();
        assert delegate != null;
        final AtomicInteger openCalls = new AtomicInteger(jsonValue.size);
        for (JsonValue childJson = jsonValue.child; childJson != null; childJson = childJson.next) {
            final Method setterMethod = behaviorDecorator.getMethodFor(childJson.name);
            if (setterMethod == null) {
                JsonConverter.LOGGER.error("<convertBehavior> unknown setter for json value " + childJson + " parent is: " + jsonValue.name);
                decrementToCallback(openCalls, delegate, callback);
                continue;
            }

            // check for entity parameters, they have to be created async
            if (!setterMethod.getParameterTypes()[0].isAssignableFrom(Entity.class)) {
                // check for plain parameters of this behavior
                JsonConverter.LOGGER.debug("<convertBehavior> setterMethod '" + setterMethod.getName() + "' for simple type ");
                setSingleProperty(graph, childJson, setterMethod, delegate, entity -> decrementToCallback(openCalls, delegate, callback));
                continue;
            }

            assert setterMethod.getParameterTypes()[0].isAssignableFrom(Entity.class);
            // method has an entity parameter which needs to be created async
            final IFactoryDecorator<IEntityCommand> entityFactory = FactoryContainer.getEntityCommand(childJson.name);
            if (entityFactory == null) {
                JsonConverter.LOGGER.error("<convertBehavior> no factory for type '" + childJson.name + "' found");
                decrementToCallback(openCalls, delegate, callback);
                continue;
            }
            // we have a factory we can call the method
            runEntityCommand(graph, childJson, entity -> {
                JsonConverter.LOGGER.debug("<convertBehavior> setting: " + entity + " for " + delegate);
                try {
                    setterMethod.invoke(delegate, entity);
                    decrementToCallback(openCalls, delegate, callback);
                } catch (final Exception ex) {
                    throw new GdxRuntimeException(ex);
                }
            });
        }
    }

    // we need to run the callback as soon as all properties are set, since some properties are set async
    // we need to count how many are set already
    private <T> void decrementToCallback(AtomicInteger openCalls, T delegate, Callback<T> callback) {
        if (openCalls.decrementAndGet() == 0) {
            callback.ready(delegate);
        }
    }

}

package net.wohlfart.pluto.entity.effects;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.scene.properties.HasBehavior;
import net.wohlfart.pluto.scene.properties.HasLightMethod;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.scene.properties.HasRotation;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;
import net.wohlfart.pluto.stage.loader.EntityProperty;
import net.wohlfart.pluto.stage.loader.EntityElement;
import net.wohlfart.pluto.util.Utils;

/*
 * implementing 3 different light types:
 * - ambient light
 * - directional light
 * - point light
 */
@EntityElement(type = "Light")
public class LightCommand implements IEntityCommand {

    protected Position position;

    protected Vector3 direction;

    protected Color color;

    protected float intensity = 100;

    protected long uid;

    public IBehavior<?> behavior;

    @Override
    public long getUid() {
        assert this.uid != IEntityCommand.NULL_UID : "uid is invalid";
        return uid;
    }

    @EntityProperty(name = "uid", type = "Long")
    public LightCommand withUid(long uid) {
        this.uid = uid;
        return this;
    }

    @Override
    public void runNow(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        assert Utils.isRenderThread();
    }

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        assert !Utils.isRenderThread();
    }

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        assert Utils.isRenderThread();
        Entity entity = null;
        if (this.direction != null && this.color != null) {
            entity = registerDirectionalLight(entityPool);
        } else if (this.position != null && this.color != null) {
            entity = registerPointLight(entityPool);
        } else if (this.color != null) {
            entity = registerAmbientLight(entityPool);
        }

        if (entity == null) {
            throw new GdxRuntimeException("missing data for light "
                    + " direction: '" + this.direction + "'"
                    + " position: '" + this.position + "'"
                    + " color: '" + this.color + "'");
        }
        entityPool.addEntity(entity);
        futureEntity.set(entity);
    }

    private Entity registerDirectionalLight(EntityPool entityPool) {
        final Entity entity = entityPool.createEntity();

        final DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.color.set(this.color);
        directionalLight.setDirection(this.direction);

        entity.add(entityPool.createComponent(HasTransformMethod.class)
                .withDirectionTransformMethod(directionalLight.direction));

        entity.add(entityPool.createComponent(HasLightMethod.class)
                .withDirectionalLight(directionalLight));

        return entity;
    }

    private Entity registerAmbientLight(EntityPool entityPool) {
        final Entity entity = entityPool.createEntity();

        entity.add(entityPool.createComponent(HasLightMethod.class)
                .withAmbientLight(new ColorAttribute(ColorAttribute.AmbientLight, this.color)));

        return entity;
    }

    private Entity registerPointLight(EntityPool entityPool) {
        final Entity entity = entityPool.createEntity();

        // TODO: default values probably not what we want
        final Model sphereModel = new ModelBuilder().createSphere(1, 1, 1, 10, 10,
                new Material(ColorAttribute.createDiffuse(color)),
                Usage.Position);

        final ModelInstance element = new ModelInstance(sphereModel);

        final PointLight pointLight = new PointLight()
                .setIntensity(intensity)
                .setColor(color);
        this.position.get(pointLight.position);

        if (behavior != null) {
            entity.add(entityPool.createComponent(HasBehavior.class)
                    .withBehavior(this.behavior));
        }

        entity.add(entityPool.createComponent(HasRenderables.class)
                .withDelegate(element));

        // not sure if we need this
        entity.add(entityPool.createComponent(HasRotation.class)
                .withRotation(new Quaternion()));

        entity.add(entityPool.createComponent(HasPosition.class)
                .withPosition(this.position));

        entity.add(entityPool.createComponent(HasLightMethod.class)
                .withPointLight(pointLight));

        entity.add(entityPool.createComponent(HasTransformMethod.class)
                .withPositionTransformMethod(pointLight.position, element.transform));

        return entity;
    }

    @EntityProperty(name = "position", type = "Position")
    public LightCommand withPosition(Position position) {
        this.position = new Position().set(position);
        return this;
    }

    public LightCommand withPosition(float x, float y, float z) {
        this.position = new Position().set(x, y, z);
        return this;
    }

    @EntityProperty(name = "direction", type = "Vector3")
    public LightCommand withDirection(Vector3 direction) {
        this.direction = new Vector3().set(direction);
        return this;
    }

    @EntityProperty(name = "color", type = "Color")
    public LightCommand withColor(Color color) {
        this.color = new Color().set(color);
        return this;
    }

    @EntityProperty(name = "behavior", type = "Behavior")
    public LightCommand withBehavior(IBehavior<?> behavior) {
        this.behavior = behavior;
        return this;
    }

    @EntityProperty(name = "intensity", type = "Float")
    public LightCommand withIntensity(float intensity) {
        this.intensity = intensity;
        return this;
    }

    @Override
    public String toString() {
        return "LightCommand ["
                + "position=" + position + ", "
                + "direction=" + direction + ", "
                + "color=" + color + ", "
                + "intensity=" + intensity + ", "
                + "uid=" + uid + ", "
                + "behavior=" + behavior
                + "]";
    }

}

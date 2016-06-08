package net.wohlfart.pluto.shader;

import javax.annotation.Nonnull;

import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;

public class SkyboxAttribute extends Attribute {
    public static final String Alias = "skybox";
    public static final long Type = Attribute.register(SkyboxAttribute.Alias);

    final TextureDescriptor<Cubemap> textureDescription;

    public SkyboxAttribute(Cubemap cubemap) {
        super(SkyboxAttribute.Type);
        this.textureDescription = new TextureDescriptor<>();
        this.textureDescription.texture = cubemap;
    }

    public SkyboxAttribute(final SkyboxAttribute copyFrom) {
        super(copyFrom.type);
        this.textureDescription = copyFrom.textureDescription;
    }

    @Override
    public Attribute copy() {
        return new SkyboxAttribute(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((textureDescription == null) ? 0 : textureDescription.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        //noinspection CastToConcreteClass
        final SkyboxAttribute other = (SkyboxAttribute) obj;
        if (textureDescription == null) {
            if (other.textureDescription != null) {
                return false;
            }
        } else if (!textureDescription.equals(other.textureDescription)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(@Nonnull Attribute o) {
        if (type != o.type) {
            return type < o.type ? -1 : 1;
        }
        //noinspection CastToConcreteClass
        final SkyboxAttribute other = (SkyboxAttribute) o;
        final int c = textureDescription.compareTo(other.textureDescription);
        if (c != 0) {
            return c;
        }
        return 0;
    }
}

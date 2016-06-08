package net.wohlfart.pluto.scene;

import java.util.Comparator;

import net.wohlfart.pluto.shader.SkyboxAttribute;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * renderables should be sorted like this:
 *   1. opaque: front to back, write to depth buffer
 *   2. skybox, no writes to depth buffer
 *   3. transparent: back to front
 */
@SuppressFBWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE",
        justification = "no need to serialize this class, or any container using this class")
public class SceneRenderableSorter implements RenderableSorter, Comparator<Renderable> {

    private final Vector3 tmpV1 = new Vector3();
    private final Vector3 tmpV2 = new Vector3();

    @Override
    public void sort(final Camera camera, final Array<Renderable> renderables) {
        // camera is always at 0/0/0
        renderables.sort(this);
    }

    @Override
    public int compare(final Renderable left, final Renderable right) {
        final int LEFT_FIRST = 1; // left needs to be rendered before right
        final int RIGHT_FIRST = -1; // left needs to be rendered after right

        final boolean blendRight = left.material.has(BlendingAttribute.Type)
                && ((BlendingAttribute) left.material.get(BlendingAttribute.Type)).blended;
        final boolean blendLeft = right.material.has(BlendingAttribute.Type)
                && ((BlendingAttribute) right.material.get(BlendingAttribute.Type)).blended;

        // non-blending always before any blending, skybox is always non-blending
        if (blendRight != blendLeft) {
            // one is blending the other isn't
            return blendLeft ? RIGHT_FIRST : LEFT_FIRST;
        }

        // both blending or both not blending at this point
        left.worldTransform.getTranslation(tmpV1);
        right.worldTransform.getTranslation(tmpV2);

        // since cam is always at 0/0/0 we just use the z values here
        if (blendRight) {
            // back to front, no skybox involved
            return Float.compare(tmpV1.z, tmpV2.z);

        } else {
            // front to back, skybox is the last non-blending
            final boolean skyboxRight = right.material.has(SkyboxAttribute.Type);
            final boolean skyboxLeft = left.material.has(SkyboxAttribute.Type);
            assert !(skyboxRight && skyboxLeft) : "can't have two skyboxes";

            if (skyboxRight) {
                return LEFT_FIRST; // skybox at 0 needs to be rendered after element at 1
            }
            if (skyboxLeft) {
                return RIGHT_FIRST; // element at 0 needs to be rendered before skybox at 1
            }

            // non-blending sort: front to back
            return Float.compare(tmpV2.z, tmpV1.z);
        }
    }
}

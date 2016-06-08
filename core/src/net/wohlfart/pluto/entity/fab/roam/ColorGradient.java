package net.wohlfart.pluto.entity.fab.roam;

import java.util.TreeSet;

import javax.annotation.Nonnull;

import com.badlogic.gdx.graphics.Color;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("serial")
@SuppressFBWarnings(value = "SE_NO_SERIALVERSIONID", justification = "never being serialized")
public class ColorGradient extends TreeSet<ColorGradient.GradientPoint> {
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "never being serialized")
    private final Color tmpColor = new Color();

    public static final ColorGradient HABITABLE_PLANET = new ColorGradient(
            new GradientPoint(0.0f, Color.BLACK),
            new GradientPoint(0.5f, Color.BLUE),
            new GradientPoint(0.6f, Color.GREEN),
            new GradientPoint(0.8f, Color.WHITE));

    public static class GradientPoint implements Comparable<GradientPoint> {

        final float position;
        final Color color;

        GradientPoint(float position, Color color) {
            this.position = position;
            this.color = color;
        }

        @Override
        public int compareTo(@Nonnull GradientPoint that) {
            return Float.compare(this.position, that.position);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((color == null) ? 0 : color.hashCode());
            result = prime * result + Float.floatToIntBits(position);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GradientPoint other = (GradientPoint) obj;
            if (color == null) {
                if (other.color != null) {
                    return false;
                }
            } else if (!color.equals(other.color)) {
                return false;
            }
            //noinspection RedundantIfStatement
            if (Float.floatToIntBits(position) != Float.floatToIntBits(other.position)) {
                return false;
            }
            return true;
        }

    }

    ColorGradient(GradientPoint... points) {
        for (final GradientPoint point : points) {
            add(point);
        }
    }

    Color pick(float position) {
        final int size = size();
        final GradientPoint pointArray[] = new GradientPoint[size];
        toArray(pointArray);

        GradientPoint left = pointArray[0];
        for (int i = 0; i < size; i++) {
            final GradientPoint next = pointArray[i];
            if (next.position > position) {
                break; // we need to stay below the next value
            }
            left = next;
        }

        GradientPoint right = pointArray[size - 1];
        for (int i = size - 1; i >= 0; i--) {
            final GradientPoint next = pointArray[i];
            if (next.position < position) {
                break; // we need to stay above the next value
            }
            right = next;
        }

        if (left == right) {
            return right.color;
        }

        float distanceLeft = 0.5f;
        if (right.position > left.position) {
            final float delta = right.position - left.position;
            distanceLeft = (position - left.position) / delta;
        }

        return tmpColor.set(left.color).lerp(right.color, distanceLeft);
    }

}

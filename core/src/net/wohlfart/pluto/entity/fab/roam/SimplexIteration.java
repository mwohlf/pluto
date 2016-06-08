package net.wohlfart.pluto.entity.fab.roam;

import com.badlogic.gdx.math.Vector3;

public class SimplexIteration implements HeightFunction {
    private final int iterations;
    private final float persistence;
    private final Vector3 frequency = new Vector3();
    private final float w;

    private final Vector3 vec = new Vector3();

    public SimplexIteration(int iterations, float persistence, float scale) {
        this(iterations, persistence, scale, 1);
    }

    public SimplexIteration(int iterations, float persistence, float scale, float w) {
        this.iterations = iterations;
        this.persistence = persistence;
        this.frequency.scl(scale);
        this.w = w;
    }

    @Override
    public float calculate(Vector3 input) {
        frequency.set(1, 1, 1);
        // decreasing the amplitude and increasing the frequency in each successive iteration
        float maxAmp = 0;
        float amp = 1;
        float noise = 0;
        for (int i = 0; i < iterations; i++) {
            vec.set(input).scl(frequency);
            noise += SimplexNoise.noise(vec.x, vec.y, vec.z, w) * amp;
            maxAmp += amp;
            amp *= persistence;
            frequency.scl(2);
        }
        // the average value of the iterations
        noise /= maxAmp;
        // return value in range [0...1]
        return (noise + 1f) / 2f;
    }

}

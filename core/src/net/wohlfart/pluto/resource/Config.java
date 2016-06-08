package net.wohlfart.pluto.resource;

import java.util.HashMap;
import java.util.Locale;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import net.wohlfart.pluto.gui.widget.Choice;

public class Config extends HashMap<String, String> {
    private static final long serialVersionUID = 1L;

    private static final String CONFIG_KEY = "config";

    private static final String LOCALE_KEY = "locales";
    private static final String DENSITY_KEY = "densities";

    // transient to make findbugs happy, we never serialize this class anyways
    private final Array<Choice<Locale>> locales = new Array<>();
    private final Array<Choice<Float>> densities = new Array<>();

    // TODO: load async?
    void load(FileHandle fileHandle) {
        locales.clear();
        densities.clear();
        final JsonValue value = new JsonReader().parse(fileHandle);

        final JsonValue config = value.get(Config.CONFIG_KEY);
        for (int i = 0; i < config.size; i++) {
            final JsonValue property = config.get(i);
            final String key = property.name;
            if (Config.LOCALE_KEY.equals(key)) {
                readLocals(property);
            } else if (Config.DENSITY_KEY.equals(key)) {
                readDensities(property);
            } else {
                Config.this.put(key, property.asString());
            }
        }
    }

    private void readLocals(Iterable<JsonValue> values) {
        for (final JsonValue property : values) {
            locales.add(new Choice<>(
                    "config.locales.label." + property.name,
                    Locale.forLanguageTag(property.asString())));
        }
    }

    private void readDensities(Iterable<JsonValue> values) {
        for (final JsonValue property : values) {
            densities.add(new Choice<>(
                    "config.densities.label." + property.name,
                    Float.parseFloat(property.asString())));
        }
    }

    public float getFloat(String key, float fallback) {
        if (containsKey(key)) {
            try {
                return Float.parseFloat(get(key));
            } catch (NumberFormatException e) {
                // ignored
            }
        }
        return fallback;
    }

    public String getString(String key, String fallback) {
        if (containsKey(key)) {
            return get(key);
        }
        return fallback;
    }

    public Array<Choice<Locale>> getLocales() {
        return locales;
    }

    public Array<Choice<Float>> getDensities() {
        // see: http://developer.android.com/guide/practices/screens_support.html
        return densities;
    }

}

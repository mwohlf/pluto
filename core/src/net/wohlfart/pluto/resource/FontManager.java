package net.wohlfart.pluto.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import net.wohlfart.pluto.Logging;

import java.util.HashMap;
import java.util.Map;

public class FontManager implements Disposable {

    public static final String OPEN_SANS_REGULAR = "font/OpenSans-Regular.ttf";

    public static final float ONE_INCH_IN_CM = 2.54f; // [cm/inch]
    public static final float ONE_CM_IN_INCH = 1f / FontManager.ONE_INCH_IN_CM; // [inch/cm]

    private static final Map<FontKey, BitmapFont> FONT_STASH = new HashMap<>();

    public enum FontKey {
        BUTTON_FONT(FontManager.OPEN_SANS_REGULAR, 30),
        TEXT_FONT(FontManager.OPEN_SANS_REGULAR, 20);

        private final String filename;
        private final float heightInDip;

        FontKey(String filename, float heightInDip) {
            this.filename = filename;
            this.heightInDip = heightInDip;
        }
    }

    @Override
    public void dispose() {
        for (final BitmapFont bitmapFont : FONT_STASH.values()) {
            // dispose the texture for the bitmapfont
            final Array<TextureRegion> regions = bitmapFont.getRegions();
            for (int i = 0; i < regions.size; i++) {
                regions.get(i).getTexture().dispose();
            }
            bitmapFont.dispose(); // probably does nothing
        }
        FONT_STASH.clear();
    }

    // FIXME: this is a hack to load ttf fonts as bitmaps
    public BitmapFont getFont(FontKey fontKey) {
        if (!FONT_STASH.containsKey(fontKey)) {
            load(fontKey);
        }
        return FONT_STASH.get(fontKey);
    }

    private void load(FontKey fontKey) {
        final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontKey.filename));
        final FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        Logging.ROOT.info("<getFont>"
                + " getPpcY(): " + Gdx.graphics.getPpcY() // 189 on htc, 38 on woodstock
                + " ,getPpcX(): " + Gdx.graphics.getPpcX()
                + " ,getHeight(): " + Gdx.graphics.getHeight()
                + " ,getWidth(): " + Gdx.graphics.getWidth()
                + " ,getDensity(): " + Gdx.graphics.getDensity() // 3 on htc, 0.6 on woodstock
        );

        // size in pixel
        param.size = Math.round(fontKey.heightInDip * Gdx.graphics.getDensity());
        final FreeTypeBitmapFontData data = new FreeTypeBitmapFontData();
        //data.setScale(Gdx.graphics.getDensity());
        data.setScale(1f);
        BitmapFont bitmapFont = generator.generateFont(param, data);
        bitmapFont.setOwnsTexture(false); // don't let a skin dispose the texture
        generator.dispose();
        FONT_STASH.put(fontKey, bitmapFont);
    }
}

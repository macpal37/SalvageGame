/*
 * ParserUtils.java
 *
 * Static functions for parsing JSON files.
 *
 * @author Walker M. White
 * @data   04/20/2020
 */
package com.xstudios.salvage.assets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.JsonValue;

/**
 *  This class is a collection of static functions to access data in a JSON file.
 */
public class ParserUtils {

    public static String safeConcatenate(String first, String second, char sep, char alt) {
        StringBuffer suffix = new StringBuffer();
        for (int ii = 0; ii < second.length(); ii++) {
            char c = second.charAt( ii );
            if (c == alt) {
                suffix.append( alt );
                suffix.append( alt );
            } else if (c == sep) {
                suffix.append( alt );
            } else {
                suffix.append( c );
            }
        }
        return first + sep + suffix.toString();
    }

    /**
     * Returns the {@link Color} represented by the given JSON entry
     *
     * A color may be (1) an array of floats 0-1, (2) a hex string,
     * or (3) a name.  The only names recognized are the constants
     * in {@link Color}.  If the contents of json do not match any
     * of these formats, this method uses defaultColor instead.
     *
     * @param json            The JSON entry to parse
     * @param defaultColor    The default color on failure
     *
     * @return the {@link Color} represented by the given JSON entry
     */
    public static Color parseColor(JsonValue json, Color defaultColor) {
        if (json == null) {
            return defaultColor;
        } else if (json.size > 0) {
            Color result = new Color();
            result.r = json.getFloat( 0 );
            result.g = json.size > 1 ? json.getFloat( 1 ) : 0;
            result.b = json.size > 2 ? json.getFloat( 2 ) : 0;
            result.a = json.size > 3 ? json.getFloat( 3 ) : 1;
            return result;
        } else {
            String code = json.asString();
            if (code != null && code.length() > 0) {
                if (code.charAt( 0 ) == '#') {
                    return Color.valueOf( code );
                } else {
                    try {
                        return (Color)Color.class.getField( code.toUpperCase() ).get(null);
                    } catch (Exception e) {
                    }
                }

            }
        }
        return defaultColor;
    }

    /**
     * Returns the {@link Texture.TextureFilter} represented by the given JSON entry
     *
     * The texture filter is specified by a string of lowercase letters. Compound
     * words are separated by spaces.  We use the OpenGL order for filter names,
     * so "linear mipmap nearest" is MipMapLinearNearest.
     *
     * @param json            The JSON entry to parse
     * @param defaultFilter   The default filter on failure
     *
     * @return the {@link Texture.TextureFilter} represented by the given JSON entry
     */
    public static Texture.TextureFilter parseFilter(JsonValue json, Texture.TextureFilter defaultFilter) {
        if (json == null || json.asString() == null) {
            return defaultFilter;
        }
        String filter = json.asString().toLowerCase();
        if (filter.equals( "linear" )) {
            return Texture.TextureFilter.Linear;
        } else if (filter.equals( "nearest" )) {
            return Texture.TextureFilter.Nearest;
        } else if (filter.equals( "nearest mipmap nearest" )) {
            return Texture.TextureFilter.MipMapNearestNearest;
        } else if (filter.equals( "linear mipmap nearest" )) {
            return Texture.TextureFilter.MipMapLinearNearest;
        } else if (filter.equals( "nearest mipmap linear" )) {
            return Texture.TextureFilter.MipMapNearestLinear;
        } else if (filter.equals( "linear mipmap linear" )) {
            return Texture.TextureFilter.MipMapLinearLinear;
        }
        return defaultFilter;
    }

    /**
     * Returns the {@link Texture.TextureWrap} represented by the given JSON entry
     *
     * The supported options are "clamped", "mirrored" and "repeated".
     *
     * @param json            The JSON entry to parse
     * @param defaultWrap     The default wrap on failure
     *
     * @return the {@link Texture.TextureWrap} represented by the given JSON entry
     */
    public static Texture.TextureWrap parseWrap(JsonValue json, Texture.TextureWrap defaultWrap) {
        if (json == null || json.asString() == null) {
            return defaultWrap;
        }
        String wrapper = json.asString().toLowerCase();
        if (wrapper.equals( "clamped" )) {
            return Texture.TextureWrap.ClampToEdge;
        } else if (wrapper.equals( "mirrored" )) {
            return Texture.TextureWrap.MirroredRepeat;
        } else if (wrapper.equals( "repeated" )) {
            return Texture.TextureWrap.Repeat;
        }
        return defaultWrap;
    }

    /**
     * Returns the {@link FreeTypeFontGenerator.Hinting} represented by the given JSON entry
     *
     * The texture filter is specified by a string of lowercase letters. Compound
     * words are separated by spaces. So "auto medium" is AutoMedium.
     *
     * @param json            The JSON entry to parse
     * @param defaultHinting  The default hinting on failure
     *
     * @return the {@link FreeTypeFontGenerator.Hinting} represented by the given JSON entry
     */
    public static FreeTypeFontGenerator.Hinting parseHinting(JsonValue json, FreeTypeFontGenerator.Hinting defaultHinting) {
        if (json == null || json.asString() == null) {
            return defaultHinting;
        }
        String hinting = json.asString().toLowerCase();
        if (hinting.equals( "auto medium" )) {
            return FreeTypeFontGenerator.Hinting.AutoMedium;
        } else if (hinting.equals( "auto full" )) {
            return FreeTypeFontGenerator.Hinting.AutoFull;
        } else if (hinting.equals( "auto slight" )) {
            return FreeTypeFontGenerator.Hinting.AutoSlight;
        } else if (hinting.equals( "medium" )) {
            return FreeTypeFontGenerator.Hinting.Medium;
        } else if (hinting.equals( "full" )) {
            return FreeTypeFontGenerator.Hinting.Full;
        } else if (hinting.equals( "slight" )) {
            return FreeTypeFontGenerator.Hinting.Slight;
        } else if (hinting.equals( "none" )) {
            return FreeTypeFontGenerator.Hinting.None;
        }
        return defaultHinting;
    }



}

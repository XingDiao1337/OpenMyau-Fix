package myau.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Font registry that manages all custom TTF fonts from /assets/myau/font/.
 * Provides static access to font renderers by name, with a fixed default size.
 */
public class FontRegistry {

    /** Default font size matching vanilla Minecraft visual size. */
    private static final int DEFAULT_SIZE = 18;

    /** Display name -> resource file name (without .ttf). null = vanilla Minecraft font. */
    private static final Map<String, String> FONTS = new LinkedHashMap<>();

    /** Cached renderers keyed by font resource file name. */
    private static final Map<String, UFontRenderer> RENDERER_CACHE = new LinkedHashMap<>();

    static {
        FONTS.put("Minecraft", null);
        FONTS.put("GoogleSans", "GoogleSans-Regular");
        FONTS.put("FuluTape", "FuluTape-2");
        FONTS.put("Pencil", "Pencil-2");
    }

    private FontRegistry() {
    }

    /**
     * @return All registered font display names.
     */
    public static String[] getFontNames() {
        return FONTS.keySet().toArray(new String[0]);
    }

    /**
     * Get a FontRenderer for the given font name.
     * "Minecraft" always returns the vanilla font renderer.
     *
     * @param name the display name of the font
     * @return the FontRenderer, falling back to vanilla if the custom font fails to load
     */
    public static FontRenderer getFontRenderer(String name) {
        if (name == null || name.equals("Minecraft")) {
            return Minecraft.getMinecraft().fontRendererObj;
        }

        String fileName = FONTS.get(name);
        if (fileName == null) {
            return Minecraft.getMinecraft().fontRendererObj;
        }

        UFontRenderer renderer = RENDERER_CACHE.get(fileName);
        if (renderer == null) {
            try {
                renderer = new UFontRenderer(fileName, DEFAULT_SIZE);
            } catch (Exception e) {
                renderer = null;
            }
            RENDERER_CACHE.put(fileName, renderer);
        }

        return renderer != null ? renderer : Minecraft.getMinecraft().fontRendererObj;
    }

    /**
     * Get the resource file name for a font display name.
     *
     * @param displayName the display name
     * @return the resource file name (without .ttf), or null for Minecraft default
     */
    public static String getFileName(String displayName) {
        return FONTS.get(displayName);
    }

    /**
     * Clear the renderer cache, forcing re-creation on next access.
     */
    public static void clearCache() {
        RENDERER_CACHE.clear();
    }
}

package myau.module.modules;

import myau.font.FontRegistry;
import myau.module.Module;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Global font manager module.
 * Allows selecting the font used by all 2D rendering modules that support custom fonts.
 */
public class FontManager extends Module {

    private static FontManager instance;

    public final ModeProperty fontMode = new ModeProperty("Font", 0, FontRegistry.getFontNames());

    public FontManager() {
        super("FontManager", false, true);
        instance = this;
    }

    /**
     * @return the FontRenderer selected by the user, or the vanilla Minecraft font if Minecraft is selected.
     */
    public static FontRenderer getFontRenderer() {
        if (instance == null) {
            return Minecraft.getMinecraft().fontRendererObj;
        }
        if (instance.fontMode.getValue() == 0) {
            return Minecraft.getMinecraft().fontRendererObj;
        }
        String fontName = FontRegistry.getFontNames()[instance.fontMode.getValue()];
        return FontRegistry.getFontRenderer(fontName);
    }

    /**
     * @return the current font display name.
     */
    public static String getCurrentFontName() {
        if (instance == null || instance.fontMode.getValue() == 0) {
            return "Minecraft";
        }
        return FontRegistry.getFontNames()[instance.fontMode.getValue()];
    }

    /**
     * @return true if a custom font is currently selected.
     */
    public static boolean isCustomFont() {
        return instance != null && instance.fontMode.getValue() != 0;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{FontRegistry.getFontNames()[fontMode.getValue()]};
    }
}

package myau.module.modules;

import myau.Myau;
import myau.event.EventTarget;
import myau.events.Render2DEvent;
import myau.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class WaterMark extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public WaterMark() {
        super("WaterMark", false, false);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) return;
        renderExhibition();
    }

    private void renderExhibition() {
        int fps = Minecraft.getDebugFPS();
        int ping = 0;

        if (mc.thePlayer != null && mc.theWorld != null) {
            if (mc.thePlayer.sendQueue != null
                    && mc.thePlayer.sendQueue.getPlayerInfo(mc.thePlayer.getUniqueID()) != null) {
                ping = mc.thePlayer.sendQueue.getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime();
            }
        }

        String exhibitionText = "O";
        String restText = "PenMyau-Fix ";
        String fpsValue = fps + "FPS";
        String pingValue = ping + "ms";

        HUD hud = (HUD) Myau.moduleManager.getModule(HUD.class);
        FontRenderer font = FontManager.getFontRenderer();

        float x = 2.0f;
        float y = 2.0f;

        GlStateManager.pushMatrix();

        long time = System.currentTimeMillis();
        int rainbowColor = hud != null ? hud.getColor(time).getRGB() : 0xFFFFFFFF;

        font.drawStringWithShadow(exhibitionText, x, y, rainbowColor);
        float currentX = x + font.getStringWidth(exhibitionText);

        int whiteColor = 0xFFFFFFFF;
        font.drawStringWithShadow(restText, currentX, y, whiteColor);
        currentX += font.getStringWidth(restText);

        int grayColor = 0xFFAAAAAA;
        font.drawStringWithShadow("[", currentX, y, grayColor);
        currentX += font.getStringWidth("[");

        font.drawStringWithShadow(fpsValue, currentX, y, whiteColor);
        currentX += font.getStringWidth(fpsValue);

        font.drawStringWithShadow("]", currentX, y, grayColor);
        currentX += font.getStringWidth("]");

        String space = " ";
        font.drawStringWithShadow(space, currentX, y, whiteColor);
        currentX += font.getStringWidth(space);

        font.drawStringWithShadow("[", currentX, y, grayColor);
        currentX += font.getStringWidth("[");

        font.drawStringWithShadow(pingValue, currentX, y, whiteColor);
        currentX += font.getStringWidth(pingValue);

        font.drawStringWithShadow("]", currentX, y, grayColor);

        GlStateManager.popMatrix();
    }
}
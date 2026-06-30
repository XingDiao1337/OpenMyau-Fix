package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.RenderLivingEvent;
import myau.mixin.IAccessorMinecraft;
import myau.mixin.IAccessorRenderManager;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Cape extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static ResourceLocation customCapeTexture = null;
    private static boolean textureLoaded = false;

    public final BooleanProperty showOwn = new BooleanProperty("Show Self", true);
    public final BooleanProperty showOthers = new BooleanProperty("Show Others", false);
    public final FloatProperty scale = new FloatProperty("Scale", 1.0F, 0.5F, 2.0F);

    public Cape() {
        super("Cape", false);
    }

    @Override
    public void onEnabled() {
        if (!textureLoaded) {
            loadCapeTexture();
        }
    }

    private void loadCapeTexture() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/assets/minecraft/myau/texture/capes/cape.png");
            if (inputStream != null) {
                BufferedImage image = ImageIO.read(inputStream);
                if (image != null) {
                    DynamicTexture dynamicTexture = new DynamicTexture(image);
                    customCapeTexture = mc.getTextureManager().getDynamicTextureLocation("myau_cape", dynamicTexture);
                    textureLoaded = true;
                }
                inputStream.close();
            } else {
                System.err.println("Cape texture not found at /assets/minecraft/myau/texture/capes/cape.png");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventTarget
    public void onRenderLiving(RenderLivingEvent event) {
        if (event.getType() != EventType.POST) return;
        if (!(event.getEntity() instanceof EntityPlayer)) return;
        if (!textureLoaded || customCapeTexture == null) return;

        EntityPlayer player = (EntityPlayer) event.getEntity();
        if (player == mc.thePlayer && !showOwn.getValue()) return;
        if (player != mc.thePlayer && !showOthers.getValue()) return;

        float partialTicks = ((IAccessorMinecraft) mc).getTimer().renderPartialTicks;
        renderCape(player, partialTicks);
    }

    private void renderCape(EntityPlayer player, float partialTicks) {
        GlStateManager.pushMatrix();

        double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double y = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

        IAccessorRenderManager renderManager = (IAccessorRenderManager) mc.getRenderManager();
        GlStateManager.translate(
                x - renderManager.getRenderPosX(),
                y - renderManager.getRenderPosY() + player.height * 0.85F,
                z - renderManager.getRenderPosZ()
        );

        float yaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

        GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);

        float scale = this.scale.getValue();

        GlStateManager.scale(0.125F * scale, 0.125F * scale, 0.125F * scale);

        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);

        GlStateManager.translate(0.0F, 0.0F, -0.2F);

        mc.getTextureManager().bindTexture(customCapeTexture);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        float capeWidth = 16.0F;
        float capeHeight = 32.0F;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0F, 0.0F);
        GL11.glVertex3f(-capeWidth / 2, capeHeight, 0.0F);
        GL11.glTexCoord2f(1.0F, 0.0F);
        GL11.glVertex3f(capeWidth / 2, capeHeight, 0.0F);
        GL11.glTexCoord2f(1.0F, 1.0F);
        GL11.glVertex3f(capeWidth / 2, 0.0F, 0.0F);
        GL11.glTexCoord2f(0.0F, 1.0F);
        GL11.glVertex3f(-capeWidth / 2, 0.0F, 0.0F);
        GL11.glEnd();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }
}
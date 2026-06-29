package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.util.KeyBindUtil;
import net.minecraft.client.Minecraft;

public class FreeLook extends Module {
    public static FreeLook INSTANCE;
    public final BooleanProperty autoF5 = new BooleanProperty("AutoF5", true);
    public boolean active = false;
    public float cameraYaw;
    public float cameraPitch;
    public float prevCameraYaw;
    public float prevCameraPitch;
    private int prevPerspective = 0;

    public FreeLook() {
        super("FreeLook", false);
        INSTANCE = this;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getType() != EventType.PRE) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        boolean keyDown = mc.currentScreen == null && KeyBindUtil.isKeyDown(this.getKey());
        if (keyDown) {
            if (!this.active) {
                this.active = true;
                this.prevPerspective = mc.gameSettings.thirdPersonView;
                if (this.autoF5.getValue()) {
                    mc.gameSettings.thirdPersonView = 1;
                }
                this.cameraYaw = mc.thePlayer.rotationYaw;
                this.cameraPitch = mc.thePlayer.rotationPitch;
                this.prevCameraYaw = this.cameraYaw;
                this.prevCameraPitch = this.cameraPitch;
            }
        } else if (this.active) {
            this.active = false;
            mc.gameSettings.thirdPersonView = this.prevPerspective;
        }

        if (this.active) {
            this.prevCameraYaw = this.cameraYaw;
            this.prevCameraPitch = this.cameraPitch;
        }
    }

    @Override
    public void onDisabled() {
        if (this.active) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.gameSettings.thirdPersonView = this.prevPerspective;
            this.active = false;
        }
    }

    public boolean isActive() {
        return this.isEnabled() && this.active;
    }
}
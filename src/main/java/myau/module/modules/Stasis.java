package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.MoveInputEvent;
import myau.events.PacketEvent;
import myau.events.UpdateEvent;
import myau.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class Stasis extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private boolean allowNextC03;

    public Stasis() {
        super("Stasis", false);
    }

    @Override
    public void onEnabled() {
        allowNextC03 = false;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null) return;
        if (event.getType() != EventType.PRE) return;
        if (mc.thePlayer.hurtTime != 0) return;
        mc.thePlayer.motionX = 0.0D;
        mc.thePlayer.motionY = 0.0D;
        mc.thePlayer.motionZ = 0.0D;
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;
        mc.thePlayer.movementInput.moveForward = 0.0F;
        mc.thePlayer.movementInput.moveStrafe = 0.0F;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;
        if (event.getType() == EventType.SEND && event.getPacket() instanceof C03PacketPlayer) {
            if (event.getPacket() instanceof C03PacketPlayer.C05PacketPlayerLook) {
                return;
            }
            if (allowNextC03) {
                allowNextC03 = false;
                return;
            }
            if (mc.thePlayer.hurtTime == 0) {
                event.setCancelled(true);
            }
        } else if (event.getType() == EventType.RECEIVE && event.getPacket() instanceof S08PacketPlayerPosLook) {
            allowNextC03 = true;
        }
    }
}
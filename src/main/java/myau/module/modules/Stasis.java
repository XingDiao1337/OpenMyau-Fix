package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.MoveInputEvent;
import myau.events.PacketEvent;
import myau.events.UpdateEvent;
import myau.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;

public class Stasis extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public Stasis() {
        super("Stasis", false);
    }

    @Override
    public void onEnabled() {
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null) return;
        if (event.getType() != EventType.PRE) return;

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
        if (!isEnabled()) return;
        if (event.getType() != EventType.SEND) return;
        if (!(event.getPacket() instanceof C03PacketPlayer)) return;
        if (event.getPacket() instanceof C03PacketPlayer.C05PacketPlayerLook) return;

        event.setCancelled(true);
    }
}
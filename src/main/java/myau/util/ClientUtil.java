package myau.util;

import myau.mixin.IAccessorMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class ClientUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isWorldLoaded() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public static Vec3 getPlayerEyesPosition() {
        return mc.thePlayer.getPositionEyes(getRenderPartialTicks());
    }

    public static float getRenderPartialTicks() {
        return ((IAccessorMinecraft) mc).getTimer().renderPartialTicks;
    }

    public static BlockPos getPlayerBlockPos() {
        return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    public static boolean isInFov(float fov, Entity entity) {
        return isInYawFov(mc.thePlayer.rotationYaw, fov, entity.posX, entity.posZ);
    }

    public static boolean isInYawFov(float yaw, float fov, double targetX, double targetZ) {
        fov *= 0.5F;
        double angleDiff = getYawDifference(yaw, targetX, targetZ);
        return Math.abs(angleDiff) <= fov;
    }

    public static double getYawDifference(float yaw, double targetX, double targetZ) {
        float targetYaw = (float)(Math.atan2(targetX - mc.thePlayer.posX, targetZ - mc.thePlayer.posZ) * 180.0 / Math.PI) * -1.0F;
        return MathHelper.wrapAngleTo180_double((double)(yaw - targetYaw));
    }
}
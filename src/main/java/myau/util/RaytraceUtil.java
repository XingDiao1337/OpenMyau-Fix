package myau.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class RaytraceUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static EntityLivingBase raycastEntity(double reach) {
        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;
        float cosYaw = MathHelper.cos(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float sinYaw = MathHelper.sin(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float cosPitch = -MathHelper.cos(-pitch * (float) (Math.PI / 180.0));
        float sinPitch = MathHelper.sin(-pitch * (float) (Math.PI / 180.0));
        Vec3 lookVec = new Vec3(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
        Vec3 reachVec = eyes.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach);

        List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().addCoord(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach).expand(1.0, 1.0, 1.0)
        );

        Entity target = null;
        double minDist = reach;
        for (Entity e : entities) {
            if (!(e instanceof EntityLivingBase) || !e.canBeCollidedWith()) continue;
            float border = e.getCollisionBorderSize();
            AxisAlignedBB aabb = e.getEntityBoundingBox().expand(border, border, border);
            MovingObjectPosition mop = aabb.calculateIntercept(eyes, reachVec);
            if (aabb.isVecInside(eyes) || mop != null) {
                double dist = eyes.distanceTo(mop != null ? mop.hitVec : eyes);
                if (dist < minDist) {
                    minDist = dist;
                    target = e;
                }
            }
        }
        return target instanceof EntityLivingBase ? (EntityLivingBase) target : null;
    }

    public static List<EntityLivingBase> getEntitiesInRay(double reach) {
        List<EntityLivingBase> result = new ArrayList<>();
        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;
        float cosYaw = MathHelper.cos(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float sinYaw = MathHelper.sin(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float cosPitch = -MathHelper.cos(-pitch * (float) (Math.PI / 180.0));
        float sinPitch = MathHelper.sin(-pitch * (float) (Math.PI / 180.0));
        Vec3 lookVec = new Vec3(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
        Vec3 reachVec = eyes.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach);

        List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().addCoord(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach).expand(1.0, 1.0, 1.0)
        );

        for (Entity e : entities) {
            if (!(e instanceof EntityLivingBase) || !e.canBeCollidedWith()) continue;
            float border = e.getCollisionBorderSize();
            AxisAlignedBB aabb = e.getEntityBoundingBox().expand(border, border, border);
            MovingObjectPosition mop = aabb.calculateIntercept(eyes, reachVec);
            if (aabb.isVecInside(eyes) || mop != null) {
                result.add((EntityLivingBase) e);
            }
        }
        return result;
    }
}
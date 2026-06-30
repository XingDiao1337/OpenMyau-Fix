package myau.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class TargetUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isInRange(Entity entity, double distance) {
        return mc.thePlayer.getPositionEyes(ClientUtil.getRenderPartialTicks()).distanceTo(RotationUtil.getEntityHitVec(entity)) <= distance;
    }

    public static List<EntityLivingBase> getLivingEntitiesInRange(double range) {
        List<Entity> all = mc.theWorld.loadedEntityList;
        List<EntityLivingBase> result = new ArrayList<>();
        for (Entity e : all) {
            if (e instanceof EntityLivingBase && e != mc.thePlayer && !e.isDead && ((EntityLivingBase) e).getHealth() > 0) {
                if (isInRange(e, range)) {
                    result.add((EntityLivingBase) e);
                }
            }
        }
        return result;
    }

    public static List<EntityLivingBase> filterEntities(List<EntityLivingBase> entities, boolean players, boolean mobs, boolean animals, boolean teamCheck, boolean botCheck) {
        List<EntityLivingBase> filtered = new ArrayList<>();
        for (EntityLivingBase e : entities) {
            if (teamCheck && TeamUtil.isTeammate(e)) continue;
            if (botCheck && e instanceof EntityPlayer && TeamUtil.isBot((EntityPlayer) e)) continue;
            if (players && e instanceof EntityPlayer) {
                filtered.add(e);
            } else if (mobs && e instanceof IMob) {
                filtered.add(e);
            } else if (animals && e instanceof IAnimals) {
                filtered.add(e);
            }
        }
        return filtered;
    }
}
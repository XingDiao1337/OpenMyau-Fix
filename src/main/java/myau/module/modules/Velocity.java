package myau.module.modules;

import com.google.common.base.CaseFormat;
import myau.Myau;
import myau.event.EventManager;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.*;
import myau.mixin.IAccessorEntity;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.ChatUtil;
import myau.util.KeyBindUtil;
import myau.util.MoveUtil;
import myau.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;

public class Velocity extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private int chanceCounter = 0;
    private boolean pendingExplosion = false;
    private boolean allowNext = true;
    private boolean jumpFlag = false;

    private int rotatoTickCounter = 0;
    private double knockbackX = 0;
    private double knockbackZ = 0;
    private float[] targetRotation = null;
    private int reduceTick = -1;
    private boolean pressed = false;
    private boolean hasReceivedVelocity = false;
    private int ticksSinceVelocity = -1;
    public static boolean extraAttacked = false;
    public static boolean velocityAttacked = false;

    private boolean shouldJump = false;
    private int jumpCooldown = 0;

    private int slapReduceTicks = 0;
    private int slapAnInt = 0;
    private boolean slot = false;
    private boolean attack = false;
    private boolean swing = false;
    private boolean block = false;
    private boolean inventory = false;
    private boolean dig = false;

    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"Vanilla", "Jump", "Hypixel", "Slap_Attack"});

    public final PercentProperty chance = new PercentProperty("chance", 100, () -> mode.getValue() == 0);
    public final PercentProperty horizontal = new PercentProperty("horizontal", 0, () -> mode.getValue() == 0);
    public final PercentProperty vertical = new PercentProperty("vertical", 100, () -> mode.getValue() == 0);
    public final PercentProperty explosionHorizontal = new PercentProperty("explosions-horizontal", 100, () -> mode.getValue() == 0);
    public final PercentProperty explosionVertical = new PercentProperty("explosions-vertical", 100, () -> mode.getValue() == 0);

    public final BooleanProperty reduce = new BooleanProperty("reduce", true, () -> mode.getValue() == 2);
    public final IntProperty attackTimes = new IntProperty("attack-times", 1, 1, 5, () -> mode.getValue() == 2 && reduce.getValue());
    private final BooleanProperty onlySprinting = new BooleanProperty("only-sprinting", true, () -> mode.getValue() == 2 && reduce.getValue());
    private final BooleanProperty reduceWhenCanAttack = new BooleanProperty("reduce-when-can-attack", true, () -> mode.getValue() == 2 && reduce.getValue());
    public final BooleanProperty jump = new BooleanProperty("jump", true, () -> mode.getValue() == 2);
    public final BooleanProperty rotate = new BooleanProperty("rotate", false, () -> mode.getValue() == 2);
    public final IntProperty rotateTick = new IntProperty("rotate-ticks", 3, 1, 12, () -> mode.getValue() == 2 && rotate.getValue());

    public final BooleanProperty slapReduce = new BooleanProperty("attack-reduce", true, () -> mode.getValue() == 3);

    public final BooleanProperty fakeCheck = new BooleanProperty("fake-check", true);
    public final BooleanProperty debugLog = new BooleanProperty("debug-log", false);

    private boolean isInLiquidOrWeb() {
        return mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || ((IAccessorEntity) mc.thePlayer).getIsInWeb();
    }

    public Velocity() {
        super("Velocity", false);
    }

    @EventTarget
    public void onKnockback(KnockbackEvent event) {
        if (!this.isEnabled() || event.isCancelled()) {
            this.pendingExplosion = false;
            this.allowNext = true;
            return;
        }

        if (!this.allowNext || !this.fakeCheck.getValue()) {
            this.allowNext = true;
            if (this.pendingExplosion) {
                if (this.mode.getValue() == 0) {
                    this.pendingExplosion = false;
                    if (this.explosionHorizontal.getValue() > 0) {
                        event.setX(event.getX() * (double) this.explosionHorizontal.getValue() / 100.0);
                        event.setZ(event.getZ() * (double) this.explosionHorizontal.getValue() / 100.0);
                    } else {
                        event.setX(mc.thePlayer.motionX);
                        event.setZ(mc.thePlayer.motionZ);
                    }
                    if (this.explosionVertical.getValue() > 0) {
                        event.setY(event.getY() * (double) this.explosionVertical.getValue() / 100.0);
                    } else {
                        event.setY(mc.thePlayer.motionY);
                    }
                }
            } else {
                if (this.mode.getValue() == 0) {
                    this.chanceCounter = (this.chanceCounter % 100) + this.chance.getValue();
                    if (this.chanceCounter >= 100) {
                        if (this.horizontal.getValue() > 0) {
                            event.setX(event.getX() * (double) this.horizontal.getValue() / 100.0);
                            event.setZ(event.getZ() * (double) this.horizontal.getValue() / 100.0);
                        } else {
                            event.setX(mc.thePlayer.motionX);
                            event.setZ(mc.thePlayer.motionZ);
                        }
                        if (this.vertical.getValue() > 0) {
                            event.setY(event.getY() * (double) this.vertical.getValue() / 100.0);
                        } else {
                            event.setY(mc.thePlayer.motionY);
                        }
                    }
                } else if (this.mode.getValue() == 1) {
                    this.jumpFlag = true;
                } else if (this.mode.getValue() == 2) {
                    if (this.rotate.getValue() && event.getY() > 0.0) {
                        this.knockbackX = event.getX();
                        this.knockbackZ = event.getZ();
                        if (Math.abs(this.knockbackX) > 0.01 || Math.abs(this.knockbackZ) > 0.01) {
                            this.rotatoTickCounter = 1;
                        }
                    }
                    this.ticksSinceVelocity = 0;
                    this.hasReceivedVelocity = true;
                }
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!this.isEnabled()) return;

        if (this.mode.getValue() == 2 && this.jump.getValue()) {
            this.handleJumpReset();
        }

        if (this.mode.getValue() == 0) {
            return;
        }

        if (this.ticksSinceVelocity >= 0) {
            this.ticksSinceVelocity++;
        }
        if (this.ticksSinceVelocity >= 10) {
            this.ticksSinceVelocity = -1;
        }
    }

    private void handleJumpReset() {
        Scaffold scaffold = (Scaffold) Myau.moduleManager.getModule(Scaffold.class);
        if (mc.thePlayer == null || mc.currentScreen instanceof GuiInventory || scaffold.isEnabled()) return;
        if (this.ticksSinceVelocity >= 0) {
            if (this.ticksSinceVelocity == 0) {
                this.pressed = mc.gameSettings.keyBindJump.isPressed();
            }
            if (this.ticksSinceVelocity <= 2 && mc.thePlayer.onGround) {
                KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
            }
        }
        if (this.ticksSinceVelocity >= 4 && this.ticksSinceVelocity <= 9) {
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), this.pressed);
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled() && this.jumpFlag && this.mode.getValue() == 1) {
            this.jumpFlag = false;
            if (mc.thePlayer.onGround && mc.thePlayer.isSprinting() && !mc.thePlayer.isPotionActive(Potion.jump) && !this.isInLiquidOrWeb()) {
                mc.thePlayer.movementInput.jump = true;
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled()) return;

        if (this.mode.getValue() == 2) {
            if (event.getType() == EventType.PRE) {
                if (this.reduce.getValue()) {
                    if (this.velocityAttacked) {
                        KillAura killAura = (KillAura) Myau.moduleManager.getModule(KillAura.class);
                        if (killAura.getTarget() != null && killAura.isEnabled()) {
                            EventManager.call(new AttackEvent(killAura.getTarget()));
                            mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                            mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(killAura.getTarget(), C02PacketUseEntity.Action.ATTACK));
                            mc.thePlayer.motionX *= 0.6D;
                            mc.thePlayer.motionZ *= 0.6D;
                            mc.thePlayer.setSprinting(false);
                        }
                        velocityAttacked = false;
                    }

                    if (this.hasReceivedVelocity) {
                        if (this.reduceTick >= this.attackTimes.getValue()) {
                            this.reduceTick = 0;
                            this.hasReceivedVelocity = false;
                        }
                        KillAura killAura = (KillAura) Myau.moduleManager.getModule(KillAura.class);
                        if (killAura.getTarget() != null) {
                            if (mc.thePlayer.isSprinting() || !this.onlySprinting.getValue()) {
                                if (!this.reduceWhenCanAttack.getValue()
                                        || (killAura.blockTick == 0 && killAura.autoBlock.getValue() == 2)
                                        || (killAura.autoBlock.getValue() == 6 && killAura.blockTick == killAura.attackTick.getValue())
                                        || (killAura.autoBlock.getValue() != 6 && killAura.autoBlock.getValue() != 2)
                                        || (killAura.autoBlock.getValue() == 5 && killAura.blockTick == 0)) {
                                    EventManager.call(new AttackEvent(killAura.getTarget()));
                                    mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                                    mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(killAura.getTarget(), C02PacketUseEntity.Action.ATTACK));
                                    mc.thePlayer.motionX *= 0.6D;
                                    mc.thePlayer.motionZ *= 0.6D;
                                    mc.thePlayer.setSprinting(false);
                                }
                            }
                        }
                        this.reduceTick++;
                    }
                }

                int maxTick = this.rotateTick.getValue();
                if (this.rotatoTickCounter > 0 && this.rotatoTickCounter <= maxTick) {
                    if (this.rotatoTickCounter == 1) {
                        double deltaX = -this.knockbackX;
                        double deltaZ = -this.knockbackZ;
                        this.targetRotation = RotationUtil.getRotationsTo(deltaX, 0, deltaZ, event.getYaw(), event.getPitch());
                    }
                    if (this.targetRotation != null) {
                        event.setRotation(this.targetRotation[0], this.targetRotation[1], 2);
                        event.setPervRotation(this.targetRotation[0], 2);
                    }
                }
            } else if (event.getType() == EventType.POST) {
                int maxTick = this.rotateTick.getValue();
                if (this.rotatoTickCounter > 0 && this.rotatoTickCounter <= maxTick) {
                    this.rotatoTickCounter++;
                    if (this.rotatoTickCounter > maxTick) {
                        this.rotatoTickCounter = 0;
                        this.targetRotation = null;
                        this.knockbackX = 0;
                        this.knockbackZ = 0;
                    }
                }
            }
        }

        if (event.getType() == EventType.POST && this.mode.getValue() == 1) {
            int hurtTime = mc.thePlayer.hurtTime;
            if (hurtTime >= 8) {
                if (jumpCooldown <= 0) {
                    shouldJump = true;
                    jumpCooldown = 2;
                }
            } else if (hurtTime <= 1) {
                shouldJump = false;
                jumpCooldown = 0;
            }
            if (shouldJump && mc.thePlayer.onGround && jumpCooldown <= 0) {
                mc.thePlayer.jump();
                shouldJump = false;
            }
            if (jumpCooldown > 0) {
                jumpCooldown--;
            }
        }

        if (this.mode.getValue() == 3 && this.slapReduce.getValue() && event.getType() == EventType.PRE) {
            if (this.slapReduceTicks > 0) {
                this.slapReduceTicks--;
                KillAura killAura = (KillAura) Myau.moduleManager.getModule(KillAura.class);
                if (killAura != null && killAura.isEnabled() && killAura.getTarget() != null) {
                    EntityLivingBase target = killAura.getTarget();
                    if (!((IAccessorEntity) mc.thePlayer).getIsInWeb() && mc.thePlayer.isSprinting() && MoveUtil.isMoving() && target != mc.thePlayer && !this.badPackets()) {
                        EventManager.call(new AttackEvent(target));
                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
                        mc.thePlayer.motionX *= 0.6;
                        mc.thePlayer.motionZ *= 0.6;
                        mc.thePlayer.setSprinting(false);
                        this.slapAnInt++;
                        if (this.debugLog.getValue()) {
                            ChatUtil.sendFormatted(Myau.clientName + "SlapAttack reduce " + this.slapAnInt);
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!this.isEnabled() || event.isCancelled()) return;

        if (this.mode.getValue() == 3 && event.getType() == EventType.SEND) {
            Packet<?> packet = event.getPacket();
            if (packet instanceof C09PacketHeldItemChange) {
                this.slot = true;
            } else if (packet instanceof C0APacketAnimation) {
                this.swing = true;
            } else if (packet instanceof C02PacketUseEntity) {
                C02PacketUseEntity useEntity = (C02PacketUseEntity) packet;
                if (useEntity.getAction() == C02PacketUseEntity.Action.ATTACK) {
                    this.attack = true;
                }
            } else if (packet instanceof C08PacketPlayerBlockPlacement) {
                this.block = true;
            } else if (packet instanceof C07PacketPlayerDigging) {
                this.block = true;
                this.dig = true;
            } else if (packet instanceof C0DPacketCloseWindow ||
                    packet instanceof C0EPacketClickWindow ||
                    (packet instanceof C16PacketClientStatus &&
                            ((C16PacketClientStatus) packet).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)) {
                this.inventory = true;
            } else if (packet instanceof C03PacketPlayer) {
                this.resetBadPackets();
            }
        }

        if (event.getType() == EventType.RECEIVE) {
            if (this.mode.getValue() == 0) {
                if (event.getPacket() instanceof S27PacketExplosion) {
                    S27PacketExplosion packet = (S27PacketExplosion) event.getPacket();
                    if (packet.func_149149_c() != 0.0F || packet.func_149144_d() != 0.0F || packet.func_149147_e() != 0.0F) {
                        this.pendingExplosion = true;
                        if (this.explosionHorizontal.getValue() == 0 || this.explosionVertical.getValue() == 0) {
                            event.setCancelled(true);
                        }
                        if (this.debugLog.getValue()) {
                            ChatUtil.sendFormatted(
                                    String.format(
                                            "%sExplosion (&otick: %d, x: %.2f, y: %.2f, z: %.2f&r)&r",
                                            Myau.clientName,
                                            mc.thePlayer.ticksExisted,
                                            mc.thePlayer.motionX + (double) packet.func_149149_c(),
                                            mc.thePlayer.motionY + (double) packet.func_149144_d(),
                                            mc.thePlayer.motionZ + (double) packet.func_149147_e()
                                    )
                            );
                        }
                    }
                }
            }

            if (event.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
                if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                    if (this.mode.getValue() == 2) {
                        this.hasReceivedVelocity = true;
                        this.ticksSinceVelocity = 0;
                    }
                    if (this.mode.getValue() == 3 && this.slapReduce.getValue()) {
                        this.slapReduceTicks = 3;
                        if (this.debugLog.getValue()) {
                            ChatUtil.sendFormatted(Myau.clientName + "Attack reduceTicks: 3");
                        }
                    }
                    if (this.debugLog.getValue()) {
                        ChatUtil.sendFormatted(
                                String.format(
                                        "%sVelocity (&otick: %d, x: %.2f, y: %.2f, z: %.2f&r)&r",
                                        Myau.clientName,
                                        mc.thePlayer.ticksExisted,
                                        (double) packet.getMotionX() / 8000.0,
                                        (double) packet.getMotionY() / 8000.0,
                                        (double) packet.getMotionZ() / 8000.0
                                )
                        );
                    }
                }
            }

            if (event.getPacket() instanceof S19PacketEntityStatus) {
                S19PacketEntityStatus packet = (S19PacketEntityStatus) event.getPacket();
                Entity entity = packet.getEntity(mc.theWorld);
                if (entity != null && entity.equals(mc.thePlayer) && packet.getOpCode() == 2) {
                    this.allowNext = false;
                }
            }
        }
    }

    private boolean badPackets() {
        return this.badPackets(false, false, false, false, false, false);
    }

    private boolean badPackets(boolean p1, boolean p2, boolean p3, boolean p4, boolean p5, boolean p6) {
        if (this.slot && !p1) return true;
        if (this.attack && !p2) return true;
        if (this.swing && !p3) return true;
        if (this.block && !p4) return true;
        if (this.inventory && !p5) return true;
        if (this.dig && !p6) return true;
        return false;
    }

    private void resetBadPackets() {
        this.slot = false;
        this.swing = false;
        this.attack = false;
        this.block = false;
        this.inventory = false;
        this.dig = false;
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.onDisabled();
    }

    @Override
    public void onEnabled() {
        this.pendingExplosion = false;
        this.allowNext = true;
        this.rotatoTickCounter = 0;
        this.targetRotation = null;
        this.knockbackX = 0;
        this.knockbackZ = 0;
        this.reduceTick = -1;
        this.hasReceivedVelocity = false;
        this.ticksSinceVelocity = -1;
        extraAttacked = false;
        velocityAttacked = false;
        this.shouldJump = false;
        this.jumpCooldown = 0;
        this.slapReduceTicks = 0;
        this.slapAnInt = 0;
        this.resetBadPackets();
    }

    @Override
    public void onDisabled() {
        this.pendingExplosion = false;
        this.allowNext = true;
        this.hasReceivedVelocity = false;
        this.rotatoTickCounter = 0;
        this.targetRotation = null;
        this.knockbackX = 0;
        this.knockbackZ = 0;
        this.reduceTick = -1;
        this.ticksSinceVelocity = -1;
        extraAttacked = false;
        velocityAttacked = false;
        this.shouldJump = false;
        this.jumpCooldown = 0;
        this.slapReduceTicks = 0;
        this.slapAnInt = 0;
        this.resetBadPackets();
    }

    @Override
    public String[] getSuffix() {
        if (this.mode.getValue() == 0) {
            return new String[]{
                    String.format("%d%%", this.horizontal.getValue()),
                    String.format("%d%%", this.vertical.getValue())
            };
        } else {
            return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
        }
    }
}
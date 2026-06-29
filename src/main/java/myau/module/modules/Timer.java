package myau.module.modules;

import myau.Myau;
import myau.enums.BlinkModules;
import myau.event.EventManager;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.event.types.Priority;
import myau.events.*;
import myau.mixin.IAccessorMinecraft;
import myau.module.Module;
import myau.property.properties.FloatProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class Timer extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final long cooldown = 100;
    private static long lastTime = 0;

    // 删除了 Mode，只保留 Speed
    public final FloatProperty speed = new FloatProperty("Speed", 1.0F, 0.0F, 10.0F);

    private boolean lastTimerKeyPressed = false;
    private double savedMotionX;
    private double savedMotionY;
    private double savedMotionZ;

    public Timer() {
        super("Timer", false);
    }

    public static boolean canToggle() {
        return System.currentTimeMillis() - lastTime > cooldown;
    }

    @Override
    public void onEnabled() {
        if (mc.thePlayer != null) {
            savedMotionX = mc.thePlayer.motionX;
            savedMotionY = mc.thePlayer.motionY;
            savedMotionZ = mc.thePlayer.motionZ;
        }
        lastTimerKeyPressed = true;

        // 如果 Speed 为 0，立即启用 Zero 模式
        if (speed.getValue() == 0.0F) {
            net.minecraft.util.Timer timer = ((IAccessorMinecraft) mc).getTimer();
            if (timer != null) {
                timer.timerSpeed = 0.0F;
            }
            Myau.blinkManager.setBlinkState(true, BlinkModules.BLINK);
        }
    }

    @Override
    public void onDisabled() {
        lastTime = System.currentTimeMillis();
        net.minecraft.util.Timer timer = ((IAccessorMinecraft) mc).getTimer();

        // 如果之前在 Zero 模式，恢复键盘状态（原有逻辑）
        if (speed.getValue() == 0.0F) {
            if (Keyboard.isCreated()) {
                while (Keyboard.next()) {
                    int eventKey = Keyboard.getEventKey();
                    boolean eventState = Keyboard.getEventKeyState();

                    if (eventKey != 0 && eventKey == getKey()) {
                        continue;
                    }
                    KeyBinding.setKeyBindState(eventKey, eventState);
                }
            }
        }

        if (timer != null) {
            timer.timerSpeed = 1.0F;
        }

        // 如果是在 Zero 模式或 Speed 为 0，关闭 Blink 并恢复运动
        if (speed.getValue() == 0.0F) {
            Myau.blinkManager.setBlinkState(false, BlinkModules.BLINK);
            if (mc.thePlayer != null) {
                mc.thePlayer.motionX = savedMotionX;
                mc.thePlayer.motionZ = savedMotionZ;
                mc.thePlayer.motionY = savedMotionY;
            }
        }
    }

    @EventTarget(Priority.HIGHEST)
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled() || event.getType() != EventType.PRE) {
            return;
        }

        // Zero 模式（Speed == 0）不做任何 Timer 修改（已在 onEnabled 中设置），但为防止被其他模块修改，可再次检查
        if (speed.getValue() == 0.0F) {
            return;
        }

        net.minecraft.util.Timer timer = ((IAccessorMinecraft) mc).getTimer();
        if (timer == null) return;

        // 正常模式
        timer.timerSpeed = speed.getValue();
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (!isEnabled()) return;

        // Zero 模式下冻结玩家运动
        if (speed.getValue() == 0.0F) {
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionY = 0.0;
            mc.thePlayer.motionZ = 0.0;
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (!isEnabled()) return;

        if (speed.getValue() == 0.0F) {
            event.setForward(0.0f);
            event.setStrafe(0.0f);
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (!isEnabled()) return;

        if (speed.getValue() == 0.0F) {
            mc.thePlayer.movementInput.moveForward = 0.0f;
            mc.thePlayer.movementInput.moveStrafe = 0.0f;
            mc.thePlayer.movementInput.jump = false;
            mc.thePlayer.movementInput.sneak = false;
        }
    }

    @EventTarget(Priority.HIGHEST)
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || speed.getValue() != 0.0F) {
            return;
        }

        int timerKey = getKey();
        boolean timerKeyPressed = timerKey != 0 && Keyboard.isKeyDown(timerKey);

        if (timerKeyPressed && !lastTimerKeyPressed && canToggle()) {
            EventManager.call(new KeyEvent(timerKey));
        }
        lastTimerKeyPressed = timerKeyPressed;

        EventManager.call(new TickEvent(EventType.PRE));

        if (mc.thePlayer != null && mc.theWorld != null) {
            UpdateEvent preEvent = new UpdateEvent(
                    EventType.PRE,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch
            );
            EventManager.call(preEvent);

            EventManager.call(new UpdateEvent(
                    EventType.POST,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch
            ));
        }

        EventManager.call(new TickEvent(EventType.POST));
    }

    @Override
    public String[] getSuffix() {
        if (speed.getValue() == 0.0F) {
            return new String[]{"Zero"};
        }
        return new String[]{String.format("%.1fx", speed.getValue())};
    }
}
package me.cortex.nvidium.mixin.minecraft;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import me.cortex.nvidium.Nvidium;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class MixinWindow {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;assertInInitPhase()V", shift = At.Shift.AFTER))
    private void headInit(WindowEventHandler eventHandler, ScreenManager monitorTracker, DisplayData settings, String videoMode, String title, CallbackInfo ci) {
        Nvidium.preWindowInit();
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL;createCapabilities()Lorg/lwjgl/opengl/GLCapabilities;", shift = At.Shift.AFTER))
    private void init(WindowEventHandler eventHandler, ScreenManager monitorTracker, DisplayData settings, String videoMode, String title, CallbackInfo ci) {
        Nvidium.checkSystemIsCapable();
        Nvidium.setupGLDebugCallback();
    }
}

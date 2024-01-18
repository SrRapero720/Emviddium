package me.cortex.nvidium.mixin.minecraft;

import me.cortex.nvidium.util.TickableManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraftClient {
    @Inject(method = "runTick", at = @At("TAIL"))
    private void tickUploadThread(boolean tick, CallbackInfo ci) {
        //TickableManager.TickAll();
    }
}

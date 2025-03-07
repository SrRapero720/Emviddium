package me.cortex.nvidium.mixin.sodium;

import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.config.EnviddiumPage;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererGetter;
import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import me.jellysquid.mods.sodium.client.gui.options.*;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

@Mixin(value = SodiumOptionsGUI.class, remap = false, priority = 99 /* stay over Embeddium++ */)
public class MixinSodiumOptionsGUI {
    @Shadow @Final private List<OptionPage> pages;

    @Inject(method = "<init>", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addNvidiumOptions(Screen prevScreen, CallbackInfo ci) {
        this.pages.add(new EnviddiumPage());
    }

    @Inject(method = "applyChanges", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void applyShaderReload(CallbackInfo ci, HashSet<OptionStorage<?>> dirtyStorages, EnumSet<OptionFlag> flags, Minecraft client) {
        if (client.level != null) {
            SodiumWorldRenderer swr = SodiumWorldRenderer.instanceNullable();
            if (swr != null) {
                NvidiumWorldRenderer pipeline = ((INvidiumWorldRendererGetter)((SodiumWorldRendererAccessor)swr).getRenderSectionManager()).getRenderer();
                if (pipeline != null)
                    pipeline.reloadShaders();
            }
        }
    }
}

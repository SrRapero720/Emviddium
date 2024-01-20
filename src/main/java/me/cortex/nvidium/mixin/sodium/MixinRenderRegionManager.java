package me.cortex.nvidium.mixin.sodium;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererSetter;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = RenderRegionManager.class, remap = false)
public abstract class MixinRenderRegionManager implements INvidiumWorldRendererSetter {
    @Shadow protected abstract void upload(CommandList commandList, RenderRegion region, List<ChunkBuildResult> results);

    @Unique private NvidiumWorldRenderer renderer;

    @Redirect(method = "upload(Lme/jellysquid/mods/sodium/client/gl/device/CommandList;Ljava/util/Iterator;)V", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/region/RenderRegionManager;upload(Lme/jellysquid/mods/sodium/client/gl/device/CommandList;Lme/jellysquid/mods/sodium/client/render/chunk/region/RenderRegion;Ljava/util/List;)V"))
    private void redirectUpload(RenderRegionManager instance, CommandList graphics, RenderRegion meshData, List<ChunkBuildResult> pass) {
        if (Nvidium.IS_ENABLED) {
            pass.forEach(renderer::uploadBuildResult);
        } else {
            this.upload(graphics, meshData, pass);
        }
    }

    @Override
    public void setWorldRenderer(NvidiumWorldRenderer renderer) {
        this.renderer = renderer;
    }
}

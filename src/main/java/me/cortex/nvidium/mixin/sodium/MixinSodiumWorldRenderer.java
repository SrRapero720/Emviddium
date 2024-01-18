package me.cortex.nvidium.mixin.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererGetter;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;
import java.util.SortedSet;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class MixinSodiumWorldRenderer implements INvidiumWorldRendererGetter {
    @Shadow private RenderSectionManager renderSectionManager;

    @Shadow
    protected static void renderBlockEntity(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher dispatcher, BlockEntity entity) {
    }

    @Inject(method = "setupTerrain", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSectionManager;needsUpdate()Z", shift = At.Shift.BEFORE))
    private void injectTerrainSetup(Camera camera, Viewport viewport, int frame, boolean spectator, boolean updateChunksImmediately, CallbackInfo ci) {
        if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
            ((INvidiumWorldRendererGetter)renderSectionManager).getRenderer().update(camera, viewport, frame, spectator);
        }
    }

    @Inject(method = "renderBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;)V", at = @At("HEAD"), cancellable = true, remap = true)
    private void overrideEntityRenderer(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher blockEntityRenderer, CallbackInfo ci) {
        if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
            ci.cancel();
            var sectionsWithEntities = ((INvidiumWorldRendererGetter)renderSectionManager).getRenderer().getSectionsWithEntities();
            for (var section : sectionsWithEntities) {
                if (section.isDisposed() || section.getCulledBlockEntities() == null)
                    continue;
                for (var entity : section.getCulledBlockEntities()) {
                    renderBlockEntity(matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x, y, z, blockEntityRenderer, entity);
                }
            }
        }
    }

    @Override
    public NvidiumWorldRenderer getRenderer() {
        if (Nvidium.IS_ENABLED) {
            return ((INvidiumWorldRendererGetter)renderSectionManager).getRenderer();
        } else {
            return null;
        }
    }
}

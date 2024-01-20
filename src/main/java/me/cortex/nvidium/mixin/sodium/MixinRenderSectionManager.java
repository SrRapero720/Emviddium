package me.cortex.nvidium.mixin.sodium;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.config.EnviddiumConfig;
import me.cortex.nvidium.managers.AsyncOcclusionTracker;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererGetter;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererSetter;
import me.cortex.nvidium.sodiumCompat.IRenderSectionExtension;
import me.cortex.nvidium.sodiumCompat.IrisCheck;
import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionManager;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Mixin(value = RenderSectionManager.class, remap = false)
public class MixinRenderSectionManager implements INvidiumWorldRendererGetter {
    @Shadow @Final private RenderRegionManager regions;
    @Shadow @Final private Long2ReferenceMap<RenderSection> sections;
    @Shadow private @NotNull Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildQueues;
    @Shadow @Final private int renderDistance;
    @Unique private NvidiumWorldRenderer renderer;
    @Unique private Frustum viewport;


    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(SodiumWorldRenderer worldRenderer, BlockRenderPassManager renderPassManager, ClientLevel world, int renderDistance, CommandList commandList, CallbackInfo ci) {
        Nvidium.IS_ENABLED = (!Nvidium.FORCE_DISABLE) && Nvidium.IS_COMPATIBLE && IrisCheck.checkIrisShouldDisable();
        if (Nvidium.IS_ENABLED) {
            if (renderer != null)
                throw new IllegalStateException("Cannot have multiple world renderers");
            renderer = new NvidiumWorldRenderer(EnviddiumConfig.asyncBFSCache?new AsyncOcclusionTracker(renderDistance, sections, world, rebuildQueues):null);
            ((INvidiumWorldRendererSetter)regions).setWorldRenderer(renderer);
        }
    }

    @Inject(method = "destroy", at = @At("TAIL"))
    private void destroy(CallbackInfo ci) {
        if (Nvidium.IS_ENABLED) {
            if (renderer == null)
                throw new IllegalStateException("Pipeline already destroyed");
            ((INvidiumWorldRendererSetter)regions).setWorldRenderer(null);
            renderer.delete();
            renderer = null;
        }
    }

    // TODO: Look if was right
    @Redirect(method = "unloadSection", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSection;delete()V"))
    private void deleteSection(RenderSection section) {
        if (Nvidium.IS_ENABLED) {
            if (EnviddiumConfig.regionKeepDistanceCache == 32) { // FIXME: is intended hardcoded?
                renderer.deleteSection(section);
            }
        }
        section.delete();
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void trackViewport(Camera camera, Frustum viewport, int frame, boolean spectator, CallbackInfo ci) {
        this.viewport = viewport;
    }

    @Inject(method = "renderLayer", at = @At("HEAD"), cancellable = true)
    public void renderLayer(ChunkRenderMatrices matrices, BlockRenderPass pass, double x, double y, double z, CallbackInfo ci) {
        if (Nvidium.IS_ENABLED) {
            ci.cancel();
            pass.startDrawing();
            if (pass == BlockRenderPass.SOLID) {
                renderer.renderFrame(viewport, matrices, x, y, z);
            } else if (pass == BlockRenderPass.TRANSLUCENT) {
                renderer.renderTranslucent();
            }
            pass.endDrawing();
        }
    }

    @Inject(method = "getDebugStrings", at = @At("HEAD"), cancellable = true)
    private void redirectDebug(CallbackInfoReturnable<Collection<String>> cir) {
        if (Nvidium.IS_ENABLED) {
            var debugStrings = new ArrayList<String>();
            renderer.addDebugInfo(debugStrings);
            cir.setReturnValue(debugStrings);
            cir.cancel();
        }
    }

    @Override
    public NvidiumWorldRenderer getRenderer() {
        return renderer;
    }

    @Inject(method = "createTerrainRenderList", at = @At("HEAD"), cancellable = true)
    private void redirectTerrainRenderList(Camera camera, Viewport viewport, int frame, boolean spectator, CallbackInfo ci) {
        if (Nvidium.IS_ENABLED && EnviddiumConfig.asyncBFSCache) {
            ci.cancel();
        }
    }

    @Redirect(method = "submitRebuildTasks", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSection;setPendingUpdate(Lme/jellysquid/mods/sodium/client/render/chunk/ChunkUpdateType;)V"))
    private void injectEnqueueFalse(RenderSection instance, ChunkUpdateType type) {
        instance.setPendingUpdate(type);
        if (Nvidium.IS_ENABLED && EnviddiumConfig.asyncBFSCache) {
            //We need to reset the fact that its been submitted to the rebuild queue from the build queue
            ((IRenderSectionExtension) instance).isSubmittedRebuild(false);
        }
    }

    @Unique
    private boolean isSectionVisibleBfs(RenderSection section) {
        //The reason why this is done is that since the bfs search is async it could be updating the frame counter with the next frame
        // while some sections that arnt updated/ticked yet still have the old frame id
        int delta = Math.abs(section.getLastVisibleFrame() - renderer.getAsyncFrameId());
        return delta <= 1;
    }

    @Inject(method = "isSectionVisible", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSection;getLastVisibleFrame()I", shift = At.Shift.BEFORE), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void redirectIsSectionVisible(int x, int y, int z, CallbackInfoReturnable<Boolean> cir, RenderSection render) {
        if (Nvidium.IS_ENABLED && EnviddiumConfig.asyncBFSCache) {
            cir.setReturnValue(isSectionVisibleBfs(render));
        }
    }

    @Inject(method = "tickVisibleRenders", at = @At("HEAD"), cancellable = true)
    private void redirectAnimatedSpriteUpdates(CallbackInfo ci) {
        if (Nvidium.IS_ENABLED && EnviddiumConfig.asyncBFSCache && SodiumClientMod.options().performance.animateOnlyVisibleTextures) {
            ci.cancel();
            var sprites = renderer.getAnimatedSpriteSet();
            if (sprites == null) {
                return;
            }
            for (var sprite : sprites) {
                SpriteUtil.markSpriteActive(sprite);
            }
        }
    }

    @Inject(method = "scheduleRebuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSection;setPendingUpdate(Lme/jellysquid/mods/sodium/client/render/chunk/ChunkUpdateType;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void instantReschedule(int x, int y, int z, boolean important, CallbackInfo ci, RenderSection section, ChunkUpdateType pendingUpdate) {
        if (Nvidium.IS_ENABLED && EnviddiumConfig.asyncBFSCache) {
            var queue = rebuildQueues.get(pendingUpdate);
            //TODO:FIXME: this might result in the section being enqueued multiple times, if this gets executed, and the async search sees it at the exactly wrong moment
            if (isSectionVisibleBfs(section) && queue.size() < pendingUpdate.getMaximumQueueSize()) {
                ((IRenderSectionExtension)section).isSubmittedRebuild(true);
                rebuildQueues.get(pendingUpdate).add(section);
            }
        }
    }
}

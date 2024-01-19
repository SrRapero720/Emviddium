package me.cortex.nvidium.sodiumCompat;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.config.EnviddiumConfig;
import me.cortex.nvidium.mixin.sodium.CompactChunkVertexAccessor;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderConstants;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderParser;
import net.minecraft.resources.ResourceLocation;

public class ShaderLoader {
    public static String parse(ResourceLocation path) {
        var builder = ShaderConstants.builder();
        if (Nvidium.IS_DEBUG) {
            builder.add("DEBUG");
        }

        for (int i = 1; i <= EnviddiumConfig.statisticsLevel.get().ordinal(); i++) {
            builder.add("STATISTICS_"+ EnviddiumConfig.StatisticsLoggingLevel.values()[i].name());
        }


        for (int i = 1; i <= EnviddiumConfig.translucentSortLevel.get().ordinal(); i++) {
            builder.add("TRANSLUCENCY_SORTING_"+ EnviddiumConfig.TranslucencySortingLevel.values()[i].name());
        }

        builder.add("TEXTURE_MAX_SCALE", String.valueOf(CompactChunkVertexAccessor.getTEXTURE_MAX_VALUE()));

        return ShaderParser.parseShader("#import <"+path.getNamespace()+":"+path.getPath()+">", builder.build());
    }
}

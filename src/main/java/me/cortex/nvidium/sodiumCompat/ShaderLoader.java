package me.cortex.nvidium.sodiumCompat;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.config.StatisticsLoggingLevel;
import me.cortex.nvidium.config.TranslucencySortingLevel;
import me.cortex.nvidium.mixin.sodium.CompactChunkVertexAccessor;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderConstants;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderConstants.Builder;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderParser;
import net.minecraft.resources.ResourceLocation;

public class ShaderLoader {
    public static String parse(ResourceLocation path) {
        var builder = ShaderConstants.builder();
        if (Nvidium.IS_DEBUG) {
            builder.add("DEBUG");
        }

        for (int i = 1; i <= Nvidium.config.statistics_level.ordinal(); i++) {
            builder.add("STATISTICS_"+StatisticsLoggingLevel.values()[i].name());
        }


        for (int i = 1; i <= Nvidium.config.translucency_sorting_level.ordinal(); i++) {
            builder.add("TRANSLUCENCY_SORTING_"+TranslucencySortingLevel.values()[i].name());
        }

        builder.add("TEXTURE_MAX_SCALE", String.valueOf(CompactChunkVertexAccessor.getTEXTURE_MAX_VALUE()));

        return ShaderParser.parseShader("#import <"+path.getNamespace()+":"+path.getPath()+">", builder.build());
    }
}

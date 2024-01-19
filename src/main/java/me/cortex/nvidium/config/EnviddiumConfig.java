package me.cortex.nvidium.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import me.cortex.nvidium.Nvidium;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod.EventBusSubscriber(modid = Nvidium.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EnviddiumConfig {
    public static final ForgeConfigSpec SPECS;

    public static final ForgeConfigSpec.IntValue extraRd;
    public static final ForgeConfigSpec.BooleanValue temporalCoherence;
    public static final ForgeConfigSpec.IntValue maxGeometryMemory;
    public static final ForgeConfigSpec.BooleanValue automaticMemoryLimit;
    public static int extraRdCache = 100;
    public static boolean temporalCoherenceCache = true;
    public static int maxGeometryMemoryCache = 2048;
    public static boolean automaticMemoryLimitCache = true;

    public static final ForgeConfigSpec.BooleanValue asyncBFS; // Best Friend Spider
    public static boolean asyncBFSCache = true;

    public static final ForgeConfigSpec.IntValue regionKeepDistance; // TODO: look if was squared
    public static int regionKeepDistanceCache = 32;

    public static final ForgeConfigSpec.EnumValue<TranslucencySortingLevel> translucentSortLevel;
    public static final ForgeConfigSpec.EnumValue<StatisticsLoggingLevel> statisticsLevel;

    static {
        var BUILDER = new ForgeConfigSpec.Builder();

        // embeddiumplus ->
        BUILDER.push("enviddium");

        extraRd = BUILDER
                //.comment("")
                .defineInRange("extraRD", 100, 0, 1000); // TODO: ASK TO CORTEX ABOUT
        temporalCoherence = BUILDER
                //.comment("")
                .define("temporalCoherence", true);
        maxGeometryMemory = BUILDER
                //.comment("")
                .defineInRange("maxGeometryMemory", 2048, 1024, 8196); // TODO: ASK TO CORTEX ABOUT MAX MEMORY, OR TRY TO GET MAX GPU MEMORY
        automaticMemoryLimit = BUILDER
                //.comment("")
                .define("automaticMemory", true);
        asyncBFS = BUILDER
                //.comment("")
                .define("asyncBFS", true);
        regionKeepDistance = BUILDER
                //.comment("")
                .defineInRange("regionKeepDistance", 32, 8, 128); // TODO: ASK TO CORTEX ABOUT MAX DISTANCE
        translucentSortLevel = BUILDER
                //.comment("")
                .defineEnum("translucentSortLevel", TranslucencySortingLevel.QUADS);
        statisticsLevel = BUILDER
                //.comment("")
                .defineEnum("statisticsLevel", StatisticsLoggingLevel.NONE);

        BUILDER.pop();

        SPECS = BUILDER.build();
    }

    public static boolean isLoaded() {
        return SPECS.isLoaded();
    }

    public static void load() {
        if (isLoaded()) return;

        Nvidium.LOGGER.warn("Force-loading Embeddium++ config");

        // FORCE LOAD
        var path = FMLPaths.CONFIGDIR.get().resolve("embeddium++.toml");
        try {
            final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();

            configData.load();
            SPECS.setConfig(configData);
            updateCache(null);
        } catch (Exception e) {
            var file = path.toFile();
            if (!file.exists()) throw new RuntimeException("Failed to read configuration file");
            if (!file.delete()) throw new RuntimeException("Failed to remove corrupted configuration file");
            load();
        }
    }

    @SubscribeEvent
    public static void updateCache(ModConfigEvent ignored) {
        extraRdCache = extraRd.get();
        temporalCoherenceCache = temporalCoherence.get();
        maxGeometryMemoryCache = maxGeometryMemory.get();
        automaticMemoryLimitCache = automaticMemoryLimit.get();

        asyncBFSCache = asyncBFS.get();

        regionKeepDistanceCache = regionKeepDistance.get();
    }

    public enum StatisticsLoggingLevel {
        NONE,
        FRUSTUM,
        REGIONS,
        SECTIONS,
        QUADS
    }

    public enum TranslucencySortingLevel {
        NONE,
        SECTIONS,
        QUADS
    }
}

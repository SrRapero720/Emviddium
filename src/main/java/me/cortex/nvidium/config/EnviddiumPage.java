package me.cortex.nvidium.config;

import com.google.common.collect.ImmutableList;
import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.sodiumCompat.NvidiumOptionFlags;
import me.jellysquid.mods.sodium.client.gui.options.*;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public class EnviddiumPage extends OptionPage {
    private static final OptionStorage<?> store = new DefaultOptionStorage();

    public EnviddiumPage() {
        super(Component.translatable("nvidium.options.pages.nvidium"), ImmutableList.copyOf(create()));
    }

    public static ImmutableList<OptionGroup> create() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, store)
                        .setName(Component.literal("Disable nvidium"))
                        .setTooltip(Component.literal("Used to disable nvidium (DOES NOT SAVE, WILL RE-ENABLE AFTER A RE-LAUNCH)"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.HIGH)
                        .setBinding((opts, value) -> Nvidium.FORCE_DISABLE = value, opts -> Nvidium.FORCE_DISABLE)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                ).build());

        if (Nvidium.IS_COMPATIBLE && !Nvidium.IS_ENABLED && !Nvidium.FORCE_DISABLE) {
            groups.add(OptionGroup.createBuilder()
                    .add(OptionImpl.createBuilder(boolean.class, store)
                            .setName(Component.literal("Nvidium disabled due to shaders being loaded"))
                            .setTooltip(Component.literal("Nvidium disabled due to shaders being loaded"))
                            .setControl(TickBoxControl::new)
                            .setImpact(OptionImpact.VARIES)
                            .setBinding((opts, value) -> {}, opts -> false)
                            .setFlags()
                            .build()
                    ).build());
        }
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, store)
                        .setName(Component.translatable("nvidium.options.region_keep_distance.name"))
                        .setTooltip(Component.translatable("nvidium.options.region_keep_distance.tooltip"))
                        .setControl(option -> new SliderControl(option, 32, 256, 1, x->Component.literal(x==32?"Vanilla":(x==256?"Keep All":x+" chunks"))))
                        .setImpact(OptionImpact.VARIES)
                        .setEnabled(Nvidium.IS_ENABLED)
                        .setBinding((opts, value) -> {
                            EnviddiumConfig.regionKeepDistance.set(value);
                            EnviddiumConfig.regionKeepDistanceCache = value;
                        }, opts -> EnviddiumConfig.regionKeepDistanceCache)
                        .setFlags()
                        .build()
                ).add(OptionImpl.createBuilder(boolean.class, store)
                        .setName(Component.translatable("nvidium.options.enable_temporal_coherence.name"))
                        .setTooltip(Component.translatable("nvidium.options.enable_temporal_coherence.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setEnabled(Nvidium.IS_ENABLED)
                        .setBinding((opts, value) -> {
                            EnviddiumConfig.temporalCoherence.set(value);
                            EnviddiumConfig.temporalCoherenceCache = value;
                        }, opts -> EnviddiumConfig.temporalCoherenceCache)
                        .setFlags()
                        .build()
                ).add(OptionImpl.createBuilder(boolean.class, store)
                        .setName(Component.translatable("nvidium.options.async_bfs.name"))
                        .setTooltip(Component.translatable("nvidium.options.async_bfs.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.HIGH)
                        .setEnabled(Nvidium.IS_ENABLED)
                        .setBinding((opts, value) -> {
                            EnviddiumConfig.asyncBFS.set(value);
                            EnviddiumConfig.asyncBFSCache = value;
                        }, opts -> EnviddiumConfig.asyncBFSCache)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                ).add(OptionImpl.createBuilder(boolean.class, store)
                        .setName(Component.translatable("nvidium.options.automatic_memory_limit.name"))
                        .setTooltip(Component.translatable("nvidium.options.automatic_memory_limit.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.VARIES)
                        .setEnabled(Nvidium.IS_ENABLED)
                        .setBinding((opts, value) -> {
                            EnviddiumConfig.automaticMemoryLimit.set(value);
                            EnviddiumConfig.automaticMemoryLimitCache = value;
                        }, opts -> EnviddiumConfig.automaticMemoryLimitCache)
                        .setFlags()
                        .build())
                .add(OptionImpl.createBuilder(int.class, store)
                        .setName(Component.translatable("nvidium.options.max_gpu_memory.name"))
                        .setTooltip(Component.translatable("nvidium.options.max_gpu_memory.tooltip"))
                        .setControl(option -> new SliderControl(option, 2048, 32768, 512, ControlValueFormatter.translateVariable("nvidium.options.mb")))
                        .setImpact(OptionImpact.VARIES)
                        .setEnabled(Nvidium.IS_ENABLED && !EnviddiumConfig.automaticMemoryLimitCache)
                        .setBinding((opts, value) -> {
                            EnviddiumConfig.maxGeometryMemory.set(value);
                            EnviddiumConfig.maxGeometryMemoryCache = value;
                        }, opts -> EnviddiumConfig.maxGeometryMemoryCache)
                        .setFlags(Nvidium.SUPPORTS_PERSISTENT_SPARSE_ADDRESSABLE_BUFFER?new OptionFlag[0]:new OptionFlag[]{OptionFlag.REQUIRES_RENDERER_RELOAD})
                        .build()
                ).add(OptionImpl.createBuilder(EnviddiumConfig.TranslucencySortingLevel.class, store)
                        .setName(Component.translatable("nvidium.options.translucency_sorting.name"))
                        .setTooltip(Component.translatable("nvidium.options.translucency_sorting.tooltip"))
                        .setControl(
                                opts -> new CyclingControl<>(
                                        opts,
                                        EnviddiumConfig.TranslucencySortingLevel.class,
                                        new Component[]{
                                                Component.translatable("nvidium.options.translucency_sorting.none"),
                                                Component.translatable("nvidium.options.translucency_sorting.sections"),
                                                Component.translatable("nvidium.options.translucency_sorting.quads")
                                        }
                                )
                        )
                        .setBinding((opts, value) -> EnviddiumConfig.translucentSortLevel.set(value), opts -> EnviddiumConfig.translucentSortLevel.get())
                        .setEnabled(Nvidium.IS_ENABLED)
                        .setImpact(OptionImpact.MEDIUM)
                        //Technically, only need to reload when going from NONE->SECTIONS
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                ).add(OptionImpl.createBuilder(EnviddiumConfig.StatisticsLoggingLevel.class, store)
                        .setName(Component.translatable("nvidium.options.statistics_level.name"))
                        .setTooltip(Component.translatable("nvidium.options.statistics_level.tooltip"))
                        .setControl(
                                opts -> new CyclingControl<>(
                                        opts,
                                        EnviddiumConfig.StatisticsLoggingLevel.class,
                                        new Component[]{
                                                Component.translatable("nvidium.options.statistics_level.none"),
                                                Component.translatable("nvidium.options.statistics_level.frustum"),
                                                Component.translatable("nvidium.options.statistics_level.regions"),
                                                Component.translatable("nvidium.options.statistics_level.sections"),
                                                Component.translatable("nvidium.options.statistics_level.quads")
                                        }
                                )
                        )
                        .setBinding((opts, value) -> EnviddiumConfig.statisticsLevel.set(value), opts -> EnviddiumConfig.statisticsLevel.get())
                        .setEnabled(Nvidium.IS_ENABLED)
                        .setImpact(OptionImpact.LOW)
                        .setFlags(NvidiumOptionFlags.REQUIRES_SHADER_RELOAD)
                        .build()
                )
                .build());
        return ImmutableList.copyOf(groups);
    }
}

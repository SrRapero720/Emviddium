package me.cortex.nvidium;

import me.cortex.nvidium.config.EnviddiumConfig;
import net.minecraft.Util;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Nvidium.ID)
public class Nvidium {
    public static final String ID = "enviddium";
    public static final String MOD_VERSION = FMLLoader.getLoadingModList().getModFileById(ID).getMods().get(0).getVersion().getQualifier();
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static boolean IS_COMPATIBLE = false;
    public static boolean IS_ENABLED = false;
    public static boolean IS_DEBUG = System.getProperty("nvidium.isDebug", "false").equals("TRUE");
    public static boolean SUPPORTS_PERSISTENT_SPARSE_ADDRESSABLE_BUFFER = true;
    public static boolean FORCE_DISABLE = false;

    public Nvidium() {
        EnviddiumConfig.load();
    }

    //TODO: basicly have the terrain be a virtual geometry buffer
    // once it gets too full, start culling via a callback task system
    // which executes a task on the gpu and calls back once its done
    // use this to then do a rasterizing check on the terrain and remove
    // the oldest regions and sections

    //TODO: ADD LODS

    public static void checkSystemIsCapable() {
        var cap = GL.getCapabilities();
        IS_COMPATIBLE = cap.GL_NV_mesh_shader &&
                cap.GL_NV_uniform_buffer_unified_memory &&
                cap.GL_NV_vertex_buffer_unified_memory &&
                cap.GL_NV_representative_fragment_test &&
                cap.GL_ARB_sparse_buffer &&
                cap.GL_NV_bindless_multi_draw_indirect;

        if (IS_COMPATIBLE) {
            LOGGER.info("All capabilities met");
        } else {
            LOGGER.warn("Not all requirements met, disabling nvidium");
        }
        if (IS_COMPATIBLE && Util.getPlatform() == Util.OS.LINUX) {
            LOGGER.warn("Linux currently uses fallback terrain buffer due to driver inconsistencies, expect increase vram usage");
            SUPPORTS_PERSISTENT_SPARSE_ADDRESSABLE_BUFFER = false;
        }

        if (IS_COMPATIBLE) {
            LOGGER.info("Enabling Nvidium");
        }
        IS_ENABLED = IS_COMPATIBLE;
    }


    public static void setupGLDebugCallback() {

    }

    public static void preWindowInit() {

    }
}

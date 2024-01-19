package me.cortex.nvidium.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.cortex.nvidium.Nvidium;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

public class NvidiumConfig {
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

    private NvidiumConfig() {}
    public static NvidiumConfig loadOrCreate() {
        var path = getConfigPath();
        if (Files.exists(path)) {
            try (FileReader reader = new FileReader(path.toFile())) {
                return GSON.fromJson(reader, NvidiumConfig.class);
            } catch (IOException e) {
                Nvidium.LOGGER.error("Could not parse config", e);
            }
        }
        return new NvidiumConfig();
    }

    public void save() {
        //Unsafe, todo: fixme! needs to be atomic! // PORTED TO FORGE CONFIG AND FIXED B)
        try {
            Files.writeString(getConfigPath(), GSON.toJson(this));
        } catch (IOException e) {
            Nvidium.LOGGER.error("Failed to write config file", e);
        }
    }

    private static Path getConfigPath() {
        return new File("").toPath();
    }
}

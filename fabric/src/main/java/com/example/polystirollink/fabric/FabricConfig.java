package com.example.polystirollink.fabric;

import com.example.polystirollink.core.ModConfig;
import com.example.polystirollink.core.PolystirolLinkCommon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricConfig implements ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String backendUrl = "https://api.polystirol.tech";

    @Override
    public String getBackendUrl() {
        return backendUrl;
    }

    public void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("polystirollink.json");
        if (Files.exists(configPath)) {
            try (var reader = Files.newBufferedReader(configPath)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json.has("backendUrl")) {
                    backendUrl = json.get("backendUrl").getAsString();
                }
            } catch (IOException e) {
                PolystirolLinkCommon.LOGGER.error("Failed to load config", e);
            }
        } else {
            save();
        }
    }

    public void save() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("polystirollink.json");
        try {
            Files.createDirectories(configPath.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("backendUrl", backendUrl);
            try (var writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            PolystirolLinkCommon.LOGGER.error("Failed to save config", e);
        }
    }
}

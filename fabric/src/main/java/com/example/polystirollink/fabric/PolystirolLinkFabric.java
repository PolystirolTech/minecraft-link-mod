package com.example.polystirollink.fabric;

import com.example.polystirollink.fabric.commands.LinkCommand;
import com.example.polystirollink.core.PolystirolLinkCommon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class PolystirolLinkFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PolystirolLinkCommon.LOGGER.info("Polystirol Link Fabric Initializing...");

        // Load and set config
        FabricConfig config = new FabricConfig();
        config.load();
        PolystirolLinkCommon.setConfig(config);
        
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LinkCommand.register(dispatcher);
        });
    }
}

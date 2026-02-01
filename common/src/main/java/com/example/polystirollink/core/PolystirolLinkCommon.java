package com.example.polystirollink.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolystirolLinkCommon {
    public static final String MODID = "polystirollink";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    
    private static ModConfig config;

    public static void setConfig(ModConfig config) {
        PolystirolLinkCommon.config = config;
    }

    public static ModConfig getConfig() {
        return config;
    }
}

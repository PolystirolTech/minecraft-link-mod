package com.example.polystirollink;

import net.neoforged.neoforge.common.ModConfigSpec;

// Config class for server-side mod configuration
public class Config {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	public static final ModConfigSpec.ConfigValue<String> BACKEND_URL = BUILDER
			.comment("Backend URL for API requests (e.g., https://api.example.com)")
			.define("backendUrl", "");

	static final ModConfigSpec SPEC = BUILDER.build();
}

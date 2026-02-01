import com.example.polystirollink.core.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

// Config class for server-side mod configuration
public class Config implements ModConfig {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	public static final ModConfigSpec.ConfigValue<String> BACKEND_URL_VAL = BUILDER
			.comment("Backend URL for API requests (e.g., https://api.example.com)")
			.define("backendUrl", "");

	static final ModConfigSpec SPEC = BUILDER.build();

	@Override
	public String getBackendUrl() {
		return BACKEND_URL_VAL.get();
	}
}

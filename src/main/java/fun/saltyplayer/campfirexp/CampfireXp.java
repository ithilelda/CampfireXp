package fun.saltyplayer.campfirexp;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CampfireXp implements ModInitializer {
	public static final String MOD_ID = "campfirexp";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing CampfireXp...");
	}
}

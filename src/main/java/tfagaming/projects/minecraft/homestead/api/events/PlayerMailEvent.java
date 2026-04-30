package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class PlayerMailEvent extends APIEvent {
	private final Region region;
	private final OfflinePlayer player;
	private final String message;

	public PlayerMailEvent(@NotNull Region region, @NotNull OfflinePlayer player, @NotNull String message) {
		this.region = region;
		this.player = player;
		this.message = message;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull OfflinePlayer getPlayer() {
		return player;
	}

	public @NotNull String getMessage() {
		return message;
	}
}

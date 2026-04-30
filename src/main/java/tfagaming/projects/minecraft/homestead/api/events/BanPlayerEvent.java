package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.models.Region;


public class BanPlayerEvent extends APIEvent {
	private final Region region;
	private final OfflinePlayer target;
	private final String reason;

	public BanPlayerEvent(@NotNull Region region, @NotNull OfflinePlayer target, String reason) {
		this.region = region;
		this.target = target;
		this.reason = reason;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull OfflinePlayer getBannedPlayer() {
		return target;
	}

	public @Nullable String getReason() {
		return reason;
	}
}

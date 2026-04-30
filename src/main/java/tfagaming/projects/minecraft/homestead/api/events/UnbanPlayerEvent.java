package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.models.Region;


public class UnbanPlayerEvent extends APIEvent {
	private final Region region;
	private final OfflinePlayer target;

	public UnbanPlayerEvent(@NotNull Region region, @NotNull OfflinePlayer target) {
		this.region = region;
		this.target = target;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull OfflinePlayer getUnbannedPlayer() {
		return target;
	}
}

package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class InvitePlayerEvent extends APIEvent {
	private final Region region;
	private final OfflinePlayer target;

	public InvitePlayerEvent(@NotNull Region region, @NotNull OfflinePlayer target) {
		this.region = region;
		this.target = target;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull OfflinePlayer getInvitedPlayer() {
		return target;
	}
}

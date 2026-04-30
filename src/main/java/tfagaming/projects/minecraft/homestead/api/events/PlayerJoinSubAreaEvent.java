package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.SubArea;


public class PlayerJoinSubAreaEvent extends APIEvent {
	private final SubArea subArea;
	private final OfflinePlayer player;

	public PlayerJoinSubAreaEvent(@NotNull SubArea subArea, @NotNull OfflinePlayer player) {
		this.subArea = subArea;
		this.player = player;
	}

	public @NotNull SubArea getSubArea() {
		return subArea;
	}

	public @NotNull OfflinePlayer getPlayer() {
		return player;
	}
}

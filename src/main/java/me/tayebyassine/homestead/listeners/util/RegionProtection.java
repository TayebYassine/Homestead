package me.tayebyassine.homestead.listeners.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;

public final class RegionProtection {
	public static boolean hasPermission(Player player,
										Chunk chunk,
										Location location,
										long flag) {
		if (Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("special-feat.ignore-region-protection-if-action-in-disabled-world") && ChunkManager.isChunkInDisabledWorld(chunk))
			return true;

		if (player == null) return true;

		if (PlayerUtility.isOperator(player)) return true;

		if (!ChunkManager.isChunkClaimed(chunk)) return true;

		Region region = ChunkManager.getRegionOwnsTheChunk(chunk);
		if (region == null) return true;

		if (region.isOwner(player)) return true;

		SubArea subArea = SubAreaManager.findSubAreaHasLocationInside(location);

		return subArea != null
				? PlayerUtility.hasPermissionFlag(region.getUniqueId(), subArea.getUniqueId(), player, flag, true)
				: PlayerUtility.hasPermissionFlag(region.getUniqueId(), player, flag, true);
	}

	public static void hasPermission(Player player,
									 Chunk chunk,
									 Location location,
									 long flag,
									 Runnable onTrue,
									 Runnable onFalse) {
		if (Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("special-feat.ignore-region-protection-if-action-in-disabled-world") && ChunkManager.isChunkInDisabledWorld(chunk)) {
			if (onTrue != null) onTrue.run();
			return;
		}

		boolean allowed = hasPermission(player, chunk, location, flag);

		if (allowed && onTrue != null) onTrue.run();
		if (!allowed && onFalse != null) onFalse.run();
	}

	private RegionProtection() {}
}

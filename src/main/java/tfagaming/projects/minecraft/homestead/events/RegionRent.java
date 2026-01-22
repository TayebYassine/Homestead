package tfagaming.projects.minecraft.homestead.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;
import java.util.Map;

public class RegionRent {
	public RegionRent(Homestead instance) {
		for (Region region : RegionsManager.getAll()) {
			final SerializableRent rent = region.getRent();

			if (rent != null && System.currentTimeMillis() > rent.getUntilAt()) {
				region.setRent(null);

				OfflinePlayer leaser = rent.getBukkitOfflinePlayer();

				if (leaser != null && leaser.isOnline()) {
					Player player = (Player) leaser;

					Map<String, String> replacements = new HashMap<String, String>();
					replacements.put("{region}", region.getName());

					PlayerUtils.sendMessage(player, 130, replacements);
				}
			}
		}

		for (SubArea subArea : SubAreasManager.getAll()) {
			final SerializableRent rent = subArea.getRent();

			if (rent != null && System.currentTimeMillis() > rent.getUntilAt()) {
				subArea.setRent(null);

				OfflinePlayer leaser = rent.getBukkitOfflinePlayer();

				if (leaser != null && leaser.isOnline()) {
					Player player = (Player) leaser;

					Map<String, String> replacements = new HashMap<String, String>();
					replacements.put("{region}", subArea.getName());

					PlayerUtils.sendMessage(player, 130, replacements);
				}
			}
		}
	}
}

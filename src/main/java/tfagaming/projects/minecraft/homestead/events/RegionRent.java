package tfagaming.projects.minecraft.homestead.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public final class RegionRent {
	private RegionRent() {
	}

	/**
	 * Trigger event for: Region Rent
	 * @param instance Homestead's instance
	 */
	public static void trigger(Homestead instance) {
		for (Region region : RegionsManager.getAll()) {
			final SerializableRent rent = region.getRent();

			if (rent != null && System.currentTimeMillis() > rent.getUntilAt()) {
				region.setRent(null);

				OfflinePlayer leaser = rent.getBukkitOfflinePlayer();

				if (leaser != null && leaser.isOnline()) {
					Player player = (Player) leaser;

					Messages.send(player, 130, new Placeholder()
							.add("{region}", region.getName())
					);
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

					// TODO Update this
					Messages.send(player, 130, new Placeholder()
							.add("{region}", subArea.getName())
					);
				}
			}
		}
	}
}

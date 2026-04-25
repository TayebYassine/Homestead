package tfagaming.projects.minecraft.homestead.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;


import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.SubArea;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;
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
		for (Region region : RegionManager.getAll()) {
			final SeRent rent = region.getRent();

			if (rent != null && System.currentTimeMillis() > rent.getUntilAt()) {
				region.setRent(null);

				OfflinePlayer renter = rent.getRenter();

				if (renter != null && renter.isOnline()) {
					Player player = (Player) renter;

					Messages.send(player, 130, new Placeholder()
							.add("{region}", region.getName())
					);
				}
			}
		}

		for (SubArea subArea : SubAreaManager.getAll()) {
			final SeRent rent = subArea.getRent();

			if (rent != null && System.currentTimeMillis() > rent.getUntilAt()) {
				subArea.setRent(null);

				OfflinePlayer renter = rent.getRenter();

				if (renter != null && renter.isOnline()) {
					Player player = (Player) renter;

					Messages.send(player, 130, new Placeholder()
							.add("{region}", subArea.getName())
					);
				}
			}
		}
	}
}

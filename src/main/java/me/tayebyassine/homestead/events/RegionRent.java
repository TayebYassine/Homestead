package me.tayebyassine.homestead.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.tools.java.Placeholder;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;

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

					Messages.send(player, "common.rent_end", region.getName());
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

					Messages.send(player, "common.rent_subarea_end", subArea.getName(), subArea.getRegionName());
				}
			}
		}
	}
}

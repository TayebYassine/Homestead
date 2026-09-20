package me.tayebyassine.homestead.events;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RentExpireEvent;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerBank;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class RegionRent {
    private RegionRent() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Trigger event for: Region Rent
     */
    public static void trigger() {
        for (Region region : RegionManager.getAll()) {
            SeRent rent = region.getRent();

            if (rent.hasRenter() && rent.isExpired()) {
                OfflinePlayer renter = rent.getRenter();
                double deposit = rent.getSecurityDeposit();

                rent.clearRenter();

                if (renter != null) {
                    if (deposit > 0) {
                        PlayerBank.deposit(renter, deposit);
                    }

                    Homestead.callEvent(new RentExpireEvent(region, null, renter, rent));

                    if (renter.isOnline()) {
                        Player player = renter.getPlayer();
                        if (player != null) {
                            Messages.send(player, "common.rent_end", region.getName());
                        }
                    }

                    OfflinePlayer owner = region.getOwner();
                    if (owner != null && owner.isOnline()) {
                        Player ownerPlayer = owner.getPlayer();
                        if (ownerPlayer != null) {
                            Messages.send(ownerPlayer, "common.rent_end", region.getName());
                        }
                    }
                }
            }
        }

        for (SubArea subArea : SubAreaManager.getAll()) {
            SeRent rent = subArea.getRent();
            Region region = subArea.getRegion();

            if (region != null && rent.hasRenter() && rent.isExpired()) {
                OfflinePlayer renter = rent.getRenter();
                double deposit = rent.getSecurityDeposit();

                rent.clearRenter();

                if (renter != null) {
                    if (deposit > 0) {
                        PlayerBank.deposit(renter, deposit);
                    }

                    Homestead.callEvent(new RentExpireEvent(region, subArea, renter, rent));

                    if (renter.isOnline()) {
                        Player player = renter.getPlayer();
                        if (player != null) {
                            Messages.send(player, "common.rent_subarea_end", subArea.getName(), subArea.getRegionName());
                        }
                    }

                    OfflinePlayer owner = subArea.getRegion().getOwner();
                    if (owner != null && owner.isOnline()) {
                        Player ownerPlayer = owner.getPlayer();
                        if (ownerPlayer != null) {
                            Messages.send(ownerPlayer, "common.rent_subarea_end", subArea.getName(), subArea.getRegionName());
                        }
                    }
                }
            }
        }
    }
}

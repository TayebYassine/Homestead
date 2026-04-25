package tfagaming.projects.minecraft.homestead.events;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionUntrustPlayerEvent;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;


import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.economy.TaxesUtility;

public final class MemberTaxes {
	private MemberTaxes() {
	}

	/**
	 * Trigger event for: Member Taxes
	 * @param instance Homestead's instance
	 */
	public static void trigger(Homestead instance) {
		for (Region region : RegionManager.getAll()) {
			double amountToPay = region.getTaxes();

			if (amountToPay == 0) {
				continue;
			}

			for (RegionMember member : MemberManager.getMembersOfRegion(region)) {
				if (member.getTaxesAt() == 0) {
					member.setTaxesAt(TaxesUtility.getNewTaxesAt());

					continue;
				}

				if (System.currentTimeMillis() >= member.getTaxesAt()) {
					OfflinePlayer targetPlayer = member.getPlayer();

					if (targetPlayer == null) {
						return;
					}

					if (PlayerBank.get(targetPlayer) >= amountToPay) {
						PlayerBank.withdraw(targetPlayer, amountToPay);
						region.depositBank(amountToPay);
						member.setTaxesAt(TaxesUtility.getNewTaxesAt());

						if (targetPlayer.isOnline()) {
							Player targetPlayerOnline = (Player) targetPlayer;

							Placeholder placeholder = new Placeholder()
									.add("{amount}", Formatter.getBalance(amountToPay))
									.add("{region}", region.getName())
									.add("{balance}", Formatter.getBalance(PlayerBank.get(targetPlayer)));

							Messages.send(targetPlayerOnline, 106, placeholder);
							Messages.send(targetPlayerOnline, 107, placeholder);
						}
					} else {
						MemberManager.removeMemberFromRegion(targetPlayer, region);

						if (targetPlayer.isOnline()) {
							Player targetPlayerOnline = (Player) targetPlayer;

							Messages.send(targetPlayerOnline, 108, new Placeholder()
									.add("{region}", region.getName())
							);
						}

						RegionManager.addNewLog(region.getUniqueId(), 5, new Placeholder()
								.add("{playername}", targetPlayer.getName())
						);

						RegionUntrustPlayerEvent _event = new RegionUntrustPlayerEvent(region, targetPlayer, targetPlayer, RegionUntrustPlayerEvent.UntrustReason.TAXES);
						Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
					}
				}
			}
		}
	}
}

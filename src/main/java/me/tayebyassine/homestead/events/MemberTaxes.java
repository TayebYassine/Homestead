package me.tayebyassine.homestead.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionMember;
import me.tayebyassine.homestead.tools.java.Formatter;
import me.tayebyassine.homestead.tools.java.Placeholder;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;
import me.tayebyassine.homestead.tools.minecraft.economy.TaxesUtility;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerBank;

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
						continue;
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

							Messages.send(targetPlayerOnline, "common.member_tax_success", placeholder);
						}
					} else {
						MemberManager.removeMemberFromRegion(targetPlayer, region);

						if (targetPlayer.isOnline()) {
							Player targetPlayerOnline = (Player) targetPlayer;

							Messages.send(targetPlayerOnline, "common.member_tax_error_cannot_pay", region.getName());
						}

						LogManager.addLog(region, null, LogManager.PredefinedLog.UNTRUST_PLAYER, targetPlayer.getName());
					}
				}
			}
		}
	}
}

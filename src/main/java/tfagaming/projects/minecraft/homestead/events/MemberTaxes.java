package tfagaming.projects.minecraft.homestead.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.other.TaxesUtils;

import java.util.HashMap;
import java.util.Map;

public final class MemberTaxes {
	private MemberTaxes() {
	}

	/**
	 * Trigger event for: Member Taxes
	 * @param instance Homestead's instance
	 */
	public static void trigger(Homestead instance) {
		for (Region region : RegionsManager.getAll()) {
			double amountToPay = region.getTaxesAmount();

			if (amountToPay == 0) {
				continue;
			}

			for (SerializableMember member : region.getMembers()) {
				if (member.getTaxesAt() == 0) {
					region.setMemberTaxesAt(member, TaxesUtils.getNewTaxesAt());

					continue;
				}

				if (System.currentTimeMillis() >= member.getTaxesAt()) {
					OfflinePlayer targetPlayer = member.getBukkitOfflinePlayer();

					if (PlayerBank.get(targetPlayer) >= amountToPay) {
						PlayerBank.withdraw(targetPlayer, amountToPay);
						region.addBalanceToBank(amountToPay);

						region.setMemberTaxesAt(member, TaxesUtils.getNewTaxesAt());

						if (targetPlayer.isOnline()) {
							Player targetPlayerOnline = (Player) targetPlayer;

							Placeholder placeholder = new Placeholder();
							placeholder.add("{amount}", Formatters.getBalance(amountToPay));
							placeholder.add("{region}", region.getName());
							placeholder.add("{balance}", Formatters.getBalance(PlayerBank.get(targetPlayer)));

							Messages.send(targetPlayerOnline, 106, placeholder);
							Messages.send(targetPlayerOnline, 107, placeholder);
						}
					} else {
						region.removeMember(targetPlayer);

						if (targetPlayer.isOnline()) {
							Player targetPlayerOnline = (Player) targetPlayer;

							Messages.send(targetPlayerOnline, 108, new Placeholder()
									.add("{region}", region.getName())
							);
						}

						Map<String, String> replacements = new HashMap<String, String>();
						replacements.put("{playername}", member.getBukkitOfflinePlayer().getName());

						// TODO Fix this
						// RegionsManager.addNewLog(region.getUniqueId(), 5, replacements);
					}
				}
			}
		}
	}
}

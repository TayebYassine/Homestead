package tfagaming.projects.minecraft.homestead.tools.minecraft.players;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;

public final class PlayerBank {
	public static double get(OfflinePlayer player) {
		if (!Homestead.vault.isEconomyReady()) {
			return 0.0;
		}

		return Homestead.vault.getEconomy().getBalance(player);
	}

	public static boolean has(OfflinePlayer player, double amount) {
		return get(player) >= amount;
	}

	public static void deposit(OfflinePlayer player, double amount) {
		if (!Homestead.vault.isEconomyReady()) {
			return;
		}

		Homestead.vault.getEconomy().depositPlayer(player, amount);
	}

	public static void withdraw(OfflinePlayer player, double amount) {
		if (!Homestead.vault.isEconomyReady() || amount <= 0) {
			return;
		}

		Homestead.vault.getEconomy().withdrawPlayer(player, amount);
	}
}

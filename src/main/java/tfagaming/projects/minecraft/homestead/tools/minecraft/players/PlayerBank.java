package tfagaming.projects.minecraft.homestead.tools.minecraft.players;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;

public final class PlayerBank {
	private PlayerBank() {
	}

	public static double get(OfflinePlayer player) {
		if (!Homestead.VAULT.isEconomyReady()) {
			return 0.0;
		}

		return Homestead.VAULT.getEconomy().getBalance(player);
	}

	public static boolean has(OfflinePlayer player, double amount) {
		return get(player) >= amount;
	}

	public static void deposit(OfflinePlayer player, double amount) {
		if (!Homestead.VAULT.isEconomyReady()) {
			return;
		}

		Homestead.VAULT.getEconomy().deposit(player, amount);
	}

	public static void withdraw(OfflinePlayer player, double amount) {
		if (!Homestead.VAULT.isEconomyReady() || amount <= 0) {
			return;
		}

		Homestead.VAULT.getEconomy().withdraw(player, amount);
	}
}

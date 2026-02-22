package tfagaming.projects.minecraft.homestead.integrations.vault;

import org.bukkit.OfflinePlayer;

public interface EconomyProvider {
	String getName();

	double getBalance(OfflinePlayer player);

	boolean has(OfflinePlayer player, double amount);

	boolean withdraw(OfflinePlayer player, double amount);

	boolean deposit(OfflinePlayer player, double amount);
}
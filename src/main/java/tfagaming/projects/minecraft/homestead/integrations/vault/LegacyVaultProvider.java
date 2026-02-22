package tfagaming.projects.minecraft.homestead.integrations.vault;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;

/**
 * Wraps the classic Vault API (net.milkbowl.vault).
 * Used on Bukkit, Spigot, Paper, Purpur, and any non-Folia server.
 */
public class LegacyVaultProvider implements EconomyProvider, PermissionsProvider {

	private final Economy economy;
	private final Permission permissions;

	public LegacyVaultProvider(Economy economy, Permission permissions) {
		this.economy = economy;
		this.permissions = permissions;
	}

	@Override
	public String getName() {
		return economy != null ? economy.getName() : "None";
	}

	@Override
	public double getBalance(OfflinePlayer player) {
		return economy.getBalance(player);
	}

	@Override
	public boolean has(OfflinePlayer player, double amount) {
		return economy.has(player, amount);
	}

	@Override
	public boolean withdraw(OfflinePlayer player, double amount) {
		EconomyResponse response = economy.withdrawPlayer(player, amount);
		return response.transactionSuccess();
	}

	@Override
	public boolean deposit(OfflinePlayer player, double amount) {
		EconomyResponse response = economy.depositPlayer(player, amount);
		return response.transactionSuccess();
	}

	@Override
	public String getPermissionsName() {
		return permissions != null ? permissions.getName() : "None";
	}

	@Override
	public boolean has(OfflinePlayer player, String permission) {
		if (permissions == null || player.getName() == null) return false;
		return permissions.playerHas((String) null, player, permission);
	}

	@Override
	public String getPrimaryGroup(OfflinePlayer player) {
		if (permissions == null || player.getName() == null) return null;
		return permissions.getPrimaryGroup(null, player);
	}

	@Override
	public String[] getGroups(OfflinePlayer player) {
		if (permissions == null || player.getName() == null) return new String[0];
		return permissions.getPlayerGroups(null, player);
	}

	@Override
	public boolean inGroup(OfflinePlayer player, String group) {
		if (permissions == null || player.getName() == null) return false;
		return permissions.playerInGroup(null, player, group);
	}

	public Economy getRawEconomy() {
		return economy;
	}

	public Permission getRawPermissions() {
		return permissions;
	}
}
package tfagaming.projects.minecraft.homestead.integrations.vault;

import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;
import net.milkbowl.vault2.helper.context.Context;
import net.milkbowl.vault2.permission.PermissionUnlocked;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Wraps the VaultUnlocked API (net.milkbowl.vault2).
 * Used on Folia servers.
 */
public class VaultUnlockedProvider implements EconomyProvider, PermissionsProvider {

	private final Economy economy;
	private final PermissionUnlocked permissions;
	private final String pluginName;

	public VaultUnlockedProvider(Economy economy, PermissionUnlocked permissions, Plugin plugin) {
		this.economy = economy;
		this.permissions = permissions;
		this.pluginName = plugin.getName();
	}

	@Override
	public String getName() {
		return economy != null ? economy.getName() : "None";
	}

	@Override
	public double getBalance(OfflinePlayer player) {
		BigDecimal balance = economy.getBalance(pluginName, player.getUniqueId());
		return balance.doubleValue();
	}

	@Override
	public boolean has(OfflinePlayer player, double amount) {
		return economy.has(pluginName, player.getUniqueId(), BigDecimal.valueOf(amount));
	}

	@Override
	public boolean withdraw(OfflinePlayer player, double amount) {
		EconomyResponse response = economy.withdraw(pluginName, player.getUniqueId(), BigDecimal.valueOf(amount));
		return response.transactionSuccess();
	}

	@Override
	public boolean deposit(OfflinePlayer player, double amount) {
		EconomyResponse response = economy.deposit(pluginName, player.getUniqueId(), BigDecimal.valueOf(amount));
		return response.transactionSuccess();
	}

	@Override
	public String getPermissionsName() {
		return permissions != null ? permissions.getName() : "None";
	}

	@Override
	public boolean has(OfflinePlayer player, String permission) {
		if (permissions == null) return false;
		return permissions.has(null, player, permission);
	}

	@Override
	public String getPrimaryGroup(OfflinePlayer player) {
		if (permissions == null) return null;
		return permissions.getGroups(null, player)[0];
	}

	@Override
	public String[] getGroups(OfflinePlayer player) {
		if (permissions == null) return new String[0];
		return permissions.getGroups(null, player);
	}

	@Override
	public boolean inGroup(OfflinePlayer player, String group) {
		if (permissions == null) return false;
		return permissions.inGroup(null, player, group);
	}

	public Economy getRawEconomy() {
		return economy;
	}

	public PermissionUnlocked getRawPermissions() {
		return permissions;
	}
}
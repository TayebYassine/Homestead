package tfagaming.projects.minecraft.homestead.integrations.vault;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import tfagaming.projects.minecraft.homestead.Homestead;

public class LegacyVaultProvider implements EconomyProvider, PermissionsProvider {
	private final Homestead plugin;
	private Economy economy;
	private Permission permissions;

	public LegacyVaultProvider(Homestead plugin) {
		this.plugin = plugin;
	}

	public boolean setupEconomy() {
		RegisteredServiceProvider<Economy> rsp = this.plugin.getServer().getServicesManager()
				.getRegistration(Economy.class);

		if (rsp == null) {
			return false;
		}

		economy = rsp.getProvider();

		return economy != null;
	}

	public boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = this.plugin.getServer().getServicesManager()
				.getRegistration(Permission.class);

		if (rsp == null) {
			return false;
		}

		permissions = rsp.getProvider();

		return permissions != null;
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

	public EconomyProvider getEconomy() {
		return this;
	}

	public PermissionsProvider getPermissions() {
		return this;
	}
}
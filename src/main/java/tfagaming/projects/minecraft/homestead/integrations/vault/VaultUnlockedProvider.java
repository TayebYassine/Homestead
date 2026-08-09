package tfagaming.projects.minecraft.homestead.integrations.vault;

import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;
import net.milkbowl.vault2.helper.context.Context;
import net.milkbowl.vault2.helper.subject.Subject;
import net.milkbowl.vault2.permission.PermissionUnlocked;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.math.BigDecimal;

public class VaultUnlockedProvider implements EconomyProvider, PermissionsProvider {
	private final Homestead plugin;
	private Economy economy;
	private PermissionUnlocked permissions;

	public VaultUnlockedProvider(Homestead plugin) {
		this.plugin = plugin;
	}

	public boolean setupEconomy() {
		RegisteredServiceProvider<Economy> rsp = this.plugin.getServer().getServicesManager().getRegistration(Economy.class);

		if (rsp == null) {
			return false;
		}

		economy = rsp.getProvider();

		return economy != null;
	}

	public boolean setupPermissions() {
		RegisteredServiceProvider<PermissionUnlocked> rsp = this.plugin.getServer().getServicesManager().getRegistration(PermissionUnlocked.class);

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
		if (economy == null) {
			return 0.0;
		}

		return economy.getBalance(plugin.getName(), player.getUniqueId()).doubleValue();
	}

	@Override
	public boolean has(OfflinePlayer player, double amount) {
		if (economy == null) {
			return false;
		}

		return economy.has(plugin.getName(), player.getUniqueId(), BigDecimal.valueOf(amount));
	}

	@Override
	public boolean withdraw(OfflinePlayer player, double amount) {
		if (economy == null) {
			return false;
		}

		EconomyResponse response = economy.withdraw(plugin.getName(), player.getUniqueId(), BigDecimal.valueOf(amount));
		return response.transactionSuccess();
	}

	@Override
	public boolean deposit(OfflinePlayer player, double amount) {
		if (economy == null) {
			return false;
		}

		EconomyResponse response = economy.deposit(plugin.getName(), player.getUniqueId(), BigDecimal.valueOf(amount));
		return response.transactionSuccess();
	}

	@Override
	public String getPermissionsName() {
		return permissions != null ? permissions.getName() : "None";
	}

	@Override
	public boolean has(OfflinePlayer player, String permission) {
		if (permissions == null) return false;
		return permissions.has(Context.GLOBAL, Subject.player(player.getUniqueId(), player.getName()), permission).asBool();
	}

	@Override
	public String getPrimaryGroup(OfflinePlayer player) {
		if (permissions == null) return null;
		return permissions.primaryGroup(Context.GLOBAL, Subject.player(player.getUniqueId(), player.getName()));
	}

	@Override
	public String[] getGroups(OfflinePlayer player) {
		if (permissions == null) return new String[0];
		return permissions.getGroups(Context.GLOBAL, Subject.player(player.getUniqueId(), player.getName()));
	}

	@Override
	public boolean inGroup(OfflinePlayer player, String group) {
		if (permissions == null) return false;
		return permissions.inGroup(Context.GLOBAL, Subject.player(player.getUniqueId(), player.getName()), group);
	}

	public EconomyProvider getEconomy() {
		return this;
	}

	public PermissionsProvider getPermissions() {
		return this;
	}
}
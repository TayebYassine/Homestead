package tfagaming.projects.minecraft.homestead.integrations;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.integrations.vault.EconomyProvider;
import tfagaming.projects.minecraft.homestead.integrations.vault.LegacyVaultProvider;
import tfagaming.projects.minecraft.homestead.integrations.vault.PermissionsProvider;
import tfagaming.projects.minecraft.homestead.integrations.vault.VaultUnlockedProvider;

/**
 * The ancient Vault plugin, doesn't support Folia.
 */
public class Vault {

	private VaultUnlockedProvider vaultUnlockedProvider;
	private LegacyVaultProvider legacyVaultProvider;

	public Vault(Homestead plugin) {

		if (Homestead.isFolia()) {
			this.vaultUnlockedProvider = new VaultUnlockedProvider(plugin);
		} else {
			this.legacyVaultProvider = new LegacyVaultProvider(plugin);
		}
	}

	public boolean setupEconomy() {
		if (Homestead.isFolia()) {
			return vaultUnlockedProvider.setupEconomy();
		} else {
			return legacyVaultProvider.setupEconomy();
		}
	}

	public boolean setupPermissions() {
		if (Homestead.isFolia()) {
			return vaultUnlockedProvider.setupPermissions();
		} else {
			return legacyVaultProvider.setupPermissions();
		}
	}

	public EconomyProvider getEconomy() {
		if (Homestead.isFolia()) {
			return vaultUnlockedProvider.getEconomy();
		} else {
			return legacyVaultProvider.getEconomy();
		}
	}

	public PermissionsProvider getPermissions() {
		if (Homestead.isFolia()) {
			return vaultUnlockedProvider.getPermissions();
		} else {
			return legacyVaultProvider.getPermissions();
		}
	}

	public boolean isEconomyReady() {
		return getEconomy() != null;
	}

	public boolean isPermissionsReady() {
		return getPermissions() != null;
	}
}

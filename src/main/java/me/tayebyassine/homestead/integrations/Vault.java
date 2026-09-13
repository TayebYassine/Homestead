package me.tayebyassine.homestead.integrations;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.integrations.vault.EconomyProvider;
import me.tayebyassine.homestead.integrations.vault.LegacyVaultProvider;
import me.tayebyassine.homestead.integrations.vault.PermissionsProvider;
import me.tayebyassine.homestead.integrations.vault.VaultUnlockedProvider;

/**
 * The ancient Vault plugin, doesn't support Folia.
 */
public class Vault {

    private static boolean IS_VAULTUNLOCKED_DETECTED = false;

    static {
        try {
            Class.forName("net.milkbowl.vault2.economy.Economy");
            Class.forName("net.milkbowl.vault2.permission.PermissionUnlocked");
            IS_VAULTUNLOCKED_DETECTED = true;
        } catch (ClassNotFoundException e) {
            IS_VAULTUNLOCKED_DETECTED = false;
        }
    }

    private VaultUnlockedProvider vaultUnlockedProvider;
    private LegacyVaultProvider legacyVaultProvider;

    public Vault(Homestead plugin) {

        if (isVaultUnlockedDetected()) {
            this.vaultUnlockedProvider = new VaultUnlockedProvider(plugin);
        } else {
            this.legacyVaultProvider = new LegacyVaultProvider(plugin);
        }
    }

    public boolean setupEconomy() {
        if (isVaultUnlockedDetected()) {
            return vaultUnlockedProvider.setupEconomy();
        } else {
            return legacyVaultProvider.setupEconomy();
        }
    }

    public boolean setupPermissions() {
        if (isVaultUnlockedDetected()) {
            return vaultUnlockedProvider.setupPermissions();
        } else {
            return legacyVaultProvider.setupPermissions();
        }
    }

    public EconomyProvider getEconomy() {
        if (isVaultUnlockedDetected()) {
            return vaultUnlockedProvider.getEconomy();
        } else {
            return legacyVaultProvider.getEconomy();
        }
    }

    public PermissionsProvider getPermissions() {
        if (isVaultUnlockedDetected()) {
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

    public static boolean isVaultUnlockedDetected() {
        return IS_VAULTUNLOCKED_DETECTED;
    }
}

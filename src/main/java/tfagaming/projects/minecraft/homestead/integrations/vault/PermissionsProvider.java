package tfagaming.projects.minecraft.homestead.integrations.vault;

import org.bukkit.OfflinePlayer;

public interface PermissionsProvider {
	String getPermissionsName();

	boolean has(OfflinePlayer player, String permission);

	String getPrimaryGroup(OfflinePlayer player);

	String[] getGroups(OfflinePlayer player);

	boolean inGroup(OfflinePlayer player, String group);
}
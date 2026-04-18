package tfagaming.projects.minecraft.homestead.integrations;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.items.ItemUtils;

import java.util.List;

public final class NexoMC {
	public static ItemStack getCustomNexoItem(String itemId) {
		if (isAvailable()) {
			ItemBuilder builder = NexoItems.itemFromId(itemId);

			return (builder != null) ? builder.build() : new ItemStack(Material.BARRIER);
		}

		return new ItemStack(Material.BARRIER);
	}

	public static ItemStack getCustomNexoItem(String itemId, String displayname, List<String> lore) {
		if (isAvailable()) {
			ItemBuilder builder = NexoItems.itemFromId(itemId);

			ItemStack item = (builder != null) ? builder.build() : new ItemStack(Material.BARRIER);

			return ItemUtils.applyMetadata(item, displayname, lore);
		}

		return new ItemStack(Material.BARRIER);
	}

	public static ItemStack getCustomNexoItem(String itemId, String displayname, List<String> lore, Placeholder placeholder) {
		if (isAvailable()) {
			ItemBuilder builder = NexoItems.itemFromId(itemId);

			ItemStack item = (builder != null) ? builder.build() : new ItemStack(Material.BARRIER);

			return ItemUtils.applyMetadata(item, displayname, lore, placeholder);
		}

		return new ItemStack(Material.BARRIER);
	}

	public static boolean isAvailable() {
		try {
			Class.forName("com.nexomc.nexo.items.ItemBuilder");

			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Nexo");

			return plugin != null && plugin.isEnabled();
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}

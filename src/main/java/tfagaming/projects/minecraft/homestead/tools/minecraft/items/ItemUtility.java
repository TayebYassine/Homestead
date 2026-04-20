package tfagaming.projects.minecraft.homestead.tools.minecraft.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility.ButtonData;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ItemUtility {
	private ItemUtility() {
	}

	private static List<String> buildLore(List<String> lore) {
		if (lore == null) return null;
		List<String> result = new ArrayList<>();
		for (String entry : lore) {
			for (String line : entry.split("\\n")) {
				if (!line.contains("&")) line = "&f" + line;
				result.add(ColorTranslator.translate(line));
			}
		}
		return result;
	}

	private static void applyMeta(ItemStack item, String displayName, List<String> lore) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		meta.setDisplayName(ColorTranslator.translate(displayName));
		List<String> built = buildLore(lore);
		if (built != null) meta.setLore(built);
		item.setItemMeta(meta);
	}

	private static List<String> applyPlaceholders(List<String> lore, Placeholder placeholder) {
		if (lore == null) return null;
		List<String> copy = new ArrayList<>(lore);
		copy.replaceAll(line -> Formatter.applyPlaceholders(line, placeholder));
		return copy;
	}

	public static ItemStack applyMetadata(ItemStack item, String displayName, List<String> lore) {
		applyMeta(item, displayName, lore);
		return item;
	}

	public static ItemStack applyMetadata(ItemStack item, String displayName, List<String> lore,
										  Placeholder placeholder) {
		return applyMetadata(
				item,
				Formatter.applyPlaceholders(displayName, placeholder),
				applyPlaceholders(lore, placeholder)
		);
	}

	public static ItemStack getItem(ButtonData data) {
		return getItem(data.getName(), data.getLore(), data.getType());
	}

	public static ItemStack getItem(ButtonData data, Placeholder placeholder) {
		return getItem(data.getName(), data.getLore(), data.getType(), placeholder);
	}

	public static ItemStack getItem(String displayName, List<String> lore, Material material) {
		if (material == null) material = Material.BARRIER;
		ItemStack item = new ItemStack(material);
		applyMeta(item, displayName, lore);
		return item;
	}

	public static ItemStack getItem(String displayName, List<String> lore, Material material,
									Placeholder placeholder) {
		return getItem(
				Formatter.applyPlaceholders(displayName, placeholder),
				applyPlaceholders(lore, placeholder),
				material
		);
	}

	public static ItemStack getPlayerHead(OfflinePlayer player) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		if (meta != null) {
			meta.setOwningPlayer(player);
			meta.setDisplayName(player.getName() + "'s Head");
			head.setItemMeta(meta);
		}
		return head;
	}

	public static ItemStack getPlayerHead(String displayName, List<String> lore, String texture) {
		ItemStack item = getCustomHeadTexture(texture);
		if (item.getType() != Material.PLAYER_HEAD) {
			throw new IllegalStateException("Failed to create a valid Player Head!");
		}
		applyMeta(item, displayName, lore);
		return item;
	}

	public static ItemStack getPlayerHead(String displayName, List<String> lore, String texture,
										  Placeholder placeholder) {
		return getPlayerHead(
				Formatter.applyPlaceholders(displayName, placeholder),
				applyPlaceholders(lore, placeholder),
				texture
		);
	}

	public static ItemStack getPlayerHead(String displayName, List<String> lore, UUID playerId) {
		ItemStack item = getPlayerHeadById(playerId);
		if (item.getType() != Material.PLAYER_HEAD) {
			throw new IllegalStateException("Failed to create a valid Player Head!");
		}
		applyMeta(item, displayName, lore);
		return item;
	}

	public static ItemStack getPlayerHead(String displayName, List<String> lore, UUID playerId,
										  Placeholder placeholder) {
		return getPlayerHead(
				Formatter.applyPlaceholders(displayName, placeholder),
				applyPlaceholders(lore, placeholder),
				playerId
		);
	}

	private static ItemStack getCustomHeadTexture(String texture) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		if (meta != null) {
			meta.setOwnerProfile(getProfile("https://textures.minecraft.net/texture/" + texture));
			head.setItemMeta(meta);
		}
		return head;
	}

	private static PlayerProfile getProfile(String url) {
		PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
		PlayerTextures textures = profile.getTextures();
		try {
			textures.setSkin(URI.create(url).toURL());
		} catch (IllegalArgumentException | MalformedURLException e) {
			throw new RuntimeException("Invalid texture URL: " + url, e);
		}
		profile.setTextures(textures);
		return profile;
	}

	private static ItemStack getPlayerHeadById(UUID playerId) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		OfflinePlayer player = Homestead.getInstance().getOfflinePlayerSync(playerId);
		if (player != null && meta != null) {
			meta.setOwningPlayer(player);
			head.setItemMeta(meta);
		}
		return head;
	}
}
package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.MapColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MapColorMenu {
	private static final Map<String, Material> COLOR_TO_GLASS_PANE = new HashMap<>();
	private final List<MapColorEntry> colorEntries;

	static {
		// Direct matches
		COLOR_TO_GLASS_PANE.put("RED", Material.RED_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("GREEN", Material.GREEN_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("BLUE", Material.BLUE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("YELLOW", Material.YELLOW_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("CYAN", Material.CYAN_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("MAGENTA", Material.MAGENTA_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("WHITE", Material.WHITE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("GRAY", Material.GRAY_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("LIGHT_GRAY", Material.LIGHT_GRAY_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("ORANGE", Material.ORANGE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("PINK", Material.PINK_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("PURPLE", Material.PURPLE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("BROWN", Material.BROWN_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("LIME", Material.LIME_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("LIGHT_BLUE", Material.LIGHT_BLUE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("DEFAULT", Material.GLASS_PANE);

		// Similar color mappings
		COLOR_TO_GLASS_PANE.put("DARK_GRAY", Material.GRAY_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("GOLD", Material.YELLOW_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("SILVER", Material.LIGHT_GRAY_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("NAVY", Material.BLUE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("TEAL", Material.CYAN_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("MAROON", Material.RED_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("OLIVE", Material.GREEN_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("CRIMSON", Material.RED_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("CORAL", Material.PINK_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("SALMON", Material.PINK_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("TURQUOISE", Material.CYAN_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("VIOLET", Material.PURPLE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("INDIGO", Material.PURPLE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("LAVENDER", Material.MAGENTA_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("PLUM", Material.PURPLE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("TAN", Material.BROWN_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("BEIGE", Material.WHITE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("MINT", Material.LIME_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("SKY_BLUE", Material.LIGHT_BLUE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("SLATE_GRAY", Material.GRAY_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("DARK_SLATE_GRAY", Material.GRAY_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("CHOCOLATE", Material.BROWN_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("PERU", Material.BROWN_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("KHAKI", Material.YELLOW_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("THISTLE", Material.MAGENTA_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("NEON_GREEN", Material.LIME_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("ELECTRIC_BLUE", Material.LIGHT_BLUE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("HOT_PINK", Material.PINK_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("BRIGHT_ORANGE", Material.ORANGE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("NEON_YELLOW", Material.YELLOW_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("NEON_PURPLE", Material.PURPLE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("DARK_RED", Material.RED_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("DARK_GREEN", Material.GREEN_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("DARK_BLUE", Material.BLUE_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("LIGHT_GREEN", Material.LIME_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("LIGHT_PINK", Material.PINK_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("LIGHT_YELLOW", Material.YELLOW_STAINED_GLASS_PANE);
		COLOR_TO_GLASS_PANE.put("LIGHT_CYAN", Material.CYAN_STAINED_GLASS_PANE);
	}

	public MapColorMenu(Player player, Region region) {
		this.colorEntries = getColorEntries();

		PaginationMenu.builder(13, 9 * 5)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(getItems(player, region))
				.fillEmptySlots()
				.goBack((_player, event) -> new MiscellaneousSettings(player, region))
				.onClick((_player, context) -> handleMapColorClick(player, region, context))
				.build()
				.open(player);
	}

	private void handleMapColorClick(Player player, Region region, PaginationMenu.ClickContext context) {
		if (context.getIndex() >= colorEntries.size()) return;

		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, 159);
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		MapColorEntry entry = colorEntries.get(context.getIndex());
		region.setMapColor(entry.colorValue);

		Homestead.getInstance().runSyncTask(() -> new MapColorMenu(player, region));
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();
		int currentColor = region.getMapColor();

		for (MapColorEntry entry : colorEntries) {
			items.add(createColorItem(entry, entry.colorValue == currentColor));
		}

		return items;
	}

	private ItemStack createColorItem(MapColorEntry entry, boolean isSelected) {
		ItemStack item = new ItemStack(entry.glassPaneMaterial);
		ItemMeta meta = item.getItemMeta();

		if (meta != null) {
			meta.setDisplayName(ColorTranslator.translate(entry.hexCode + entry.displayName));
			item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

			item.setItemMeta(meta);
		}

		return item;
	}

	private List<MapColorEntry> getColorEntries() {
		List<MapColorEntry> entries = new ArrayList<>();

		List<String> colorNames = MapColor.getAll();

		for (String colorName : colorNames) {
			String upperName = colorName.toUpperCase().replace("-", "_");
			int colorValue = MapColor.parseFromString(colorName);
			Material glassPane = COLOR_TO_GLASS_PANE.getOrDefault(upperName, Material.GLASS_PANE);
			String displayName = MapColor.fromInt(colorValue);
			String hexCode = "&#" + String.format("%06X", colorValue & 0x00FFFFFF);

			entries.add(new MapColorEntry(colorName, colorValue, glassPane, displayName, hexCode));
		}

		return entries;
	}

	private static class MapColorEntry {
		final String colorName;
		final int colorValue;
		final Material glassPaneMaterial;
		final String displayName;
		final String hexCode;

		MapColorEntry(String colorName, int colorValue, Material glassPaneMaterial, String displayName, String hexCode) {
			this.colorName = colorName;
			this.colorValue = colorValue;
			this.glassPaneMaterial = glassPaneMaterial;
			this.displayName = displayName;
			this.hexCode = hexCode;
		}
	}
}
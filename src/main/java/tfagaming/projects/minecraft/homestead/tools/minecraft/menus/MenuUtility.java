package tfagaming.projects.minecraft.homestead.tools.minecraft.menus;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.integrations.NexoMC;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.resources.files.MenusFile;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.items.ItemUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MenuUtility {
	private MenuUtility() {
	}

	public static String getTitle(int path) {
		return Resources.<MenusFile>get(ResourceType.Menus).getString("titles." + path);
	}

	public static ButtonData getButtonData(int path) {
		String name = Resources.<MenusFile>get(ResourceType.Menus).getString("buttons." + path + ".name");
		List<String> lore = Resources.<MenusFile>get(ResourceType.Menus).getStringList("buttons." + path + ".lore");
		String type = Resources.<MenusFile>get(ResourceType.Menus).getString("buttons." + path + ".type");

		if (name == null) name = "UNDEFINED";
		if (lore == null) lore = new ArrayList<>();
		if (type == null) type = "BARRIER";

		return new ButtonData(name, lore, type);
	}

	public static ItemStack getButton(ButtonData data) {
		return resolveItem(data, data.getOriginalType(), new Placeholder(), null);
	}

	public static ItemStack getButton(ButtonData data, Placeholder placeholder) {
		return resolveItem(data, data.getOriginalType(), placeholder, null);
	}

	public static ItemStack getButton(int path) {
		return getButton(path, new Placeholder(), null);
	}

	public static ItemStack getButton(int path, OfflinePlayer playerHead) {
		return getButton(path, new Placeholder(), playerHead);
	}

	public static ItemStack getButton(int path, Placeholder placeholder) {
		return resolveItem(getButtonData(path), null, placeholder, null);
	}

	public static ItemStack getButton(int path, Placeholder placeholder, OfflinePlayer playerHead) {
		return resolveItem(getButtonData(path), null, placeholder, playerHead);
	}

	public static ItemStack getFlagButton(String flag, boolean value) {
		Placeholder placeholder = new Placeholder();

		Object description = Resources.<LanguageFile>get(ResourceType.Language)
				.getRaw("flags-info." + flag + ".description");

		placeholder.add("{flag}", flag);

		if (description instanceof String s) {
			placeholder.add("{flag-description}", s);
		} else if (description instanceof List<?> list) {
			placeholder.add("{flag-description}",
					list.stream().map(String::valueOf).collect(Collectors.joining("\n")));
		}

		placeholder.add("{state}", Formatter.getFlagState(value));
		placeholder.add("{flag-allowed}",
				Formatter.getBoolean(!Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flag)));

		String type = Resources.<LanguageFile>get(ResourceType.Language)
				.getString("flags-info." + flag + ".type");

		return resolveItem(getButtonData(17), type, placeholder, null);
	}

	public static ItemStack getBackButton() {
		return getButton(0);
	}

	public static ItemStack getPreviousPageButton() {
		return getButton(1);
	}

	public static ItemStack getNextPageButton() {
		return getButton(2);
	}

	public static ItemStack getEmptySlot() {
		return getButton(3);
	}

	private static ItemStack resolveItem(ButtonData data, String typeOverride,
										 Placeholder placeholder, OfflinePlayer player) {
		String type = typeOverride != null ? typeOverride : data.getOriginalType();
		if (type == null) type = "BARRIER";

		if (type.startsWith("PLAYERHEAD-")) {
			String texture = type.split("-", 2)[1];
			if (texture.equalsIgnoreCase("this")) {
				return player != null
						? ItemUtility.getPlayerHead(data.getName(), data.getLore(), player.getUniqueId(), placeholder)
						: ItemUtility.getItem(data.getName(), data.getLore(), Material.BARRIER, placeholder);
			}
			return ItemUtility.getPlayerHead(data.getName(), data.getLore(), texture, placeholder);
		}

		if (type.startsWith("NEXOMC-") || type.startsWith("NEXO-")) {
			String itemId = type.split("-", 2)[1];
			return NexoMC.getNexoItem(itemId, data.getName(), data.getLore(), placeholder);
		}

		Material material = Material.getMaterial(type);
		return ItemUtility.getItem(data.getName(), data.getLore(),
				material != null ? material : Material.BARRIER, placeholder);
	}

	public static final class ButtonData {
		private final String name;
		private final List<String> lore;
		private final Material type;
		private String originalType;

		public ButtonData(String name, List<String> lore, String type) {
			this.name = name;
			this.lore = lore;
			this.originalType = type;
			this.type = Material.getMaterial(type);
		}

		public String getName() {
			return name;
		}

		public List<String> getLore() {
			return lore;
		}

		public String getOriginalType() {
			return originalType;
		}

		public void setOriginalType(String type) {
			this.originalType = type;
		}

		public Material getType() {
			return type != null ? type : Material.BARRIER;
		}
	}
}
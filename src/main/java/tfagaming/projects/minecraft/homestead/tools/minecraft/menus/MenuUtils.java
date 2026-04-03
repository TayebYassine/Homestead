package tfagaming.projects.minecraft.homestead.tools.minecraft.menus;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.resources.files.MenusFile;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.items.ItemUtils;

import java.util.ArrayList;
import java.util.List;

public class MenuUtils {
	public static String getTitle(int path) {
		return Resources.<MenusFile>get(ResourceType.Menus).getString("titles." + path);
	}

	public static ButtonData getButtonData(int path) {
		String name = Resources.<MenusFile>get(ResourceType.Menus).getString("buttons." + path + ".name");
		List<String> lore = Resources.<MenusFile>get(ResourceType.Menus).getStringList("buttons." + path + ".lore");
		String type = Resources.<MenusFile>get(ResourceType.Menus).getString("buttons." + path + ".type");

		if (name == null)
			name = "NO NAME";
		if (lore == null)
			lore = new ArrayList<>();
		if (type == null)
			type = "BARRIER";

		return new ButtonData(name, lore, type);
	}

	public static ItemStack getButton(ButtonData data, Placeholder placeholder) {
		if (data.getOriginalType().startsWith("PLAYERHEAD-")) {
			String texture = data.getOriginalType().split("-")[1];

			return ItemUtils.getPlayerHead(data.getName(), data.getLore(), texture, placeholder);
		} else {
			return ItemUtils.getItem(data.getName(), data.getLore(), data.getType(), placeholder);
		}
	}

	public static ItemStack getButton(int path, OfflinePlayer... playerHead) {
		ButtonData data = getButtonData(path);

		if (data.getOriginalType().startsWith("PLAYERHEAD-")) {
			String texture = data.getOriginalType().split("-")[1];

			if (texture.equalsIgnoreCase("this")) {
				if (playerHead.length == 0) {
					return ItemUtils.getItem(data.getName(), data.getLore(), Material.BARRIER);
				} else {
					return ItemUtils.getPlayerHead(data.getName(), data.getLore(), playerHead[0].getUniqueId());
				}
			} else {
				return ItemUtils.getPlayerHead(data.getName(), data.getLore(), texture);
			}
		} else {
			return ItemUtils.getItem(data.getName(), data.getLore(), data.getType());
		}
	}

	public static ItemStack getButton(int path, Placeholder placeholder, OfflinePlayer... playerHead) {
		ButtonData data = getButtonData(path);

		if (data.getOriginalType().startsWith("PLAYERHEAD-")) {
			String texture = data.getOriginalType().split("-")[1];

			if (texture.equalsIgnoreCase("this")) {
				if (playerHead.length == 0) {
					return ItemUtils.getItem(data.getName(), data.getLore(), Material.BARRIER, placeholder);
				} else {
					return ItemUtils.getPlayerHead(data.getName(), data.getLore(), playerHead[0].getUniqueId(),
							placeholder);
				}
			} else {
				return ItemUtils.getPlayerHead(data.getName(), data.getLore(), texture, placeholder);
			}
		} else {
			return ItemUtils.getItem(data.getName(), data.getLore(), data.getType(), placeholder);
		}
	}

	@SuppressWarnings("unchecked")
	public static ItemStack getFlagButton(String flag, boolean value) {
		Placeholder placeholder = new Placeholder();

		Object description = Resources.<LanguageFile>get(ResourceType.Language).getRaw("flags-info." + flag + ".description");

		placeholder.add("{flag}", flag);

		if (description instanceof String) {
			placeholder.add("{flag-description}", description.toString());
		} else if (description instanceof List<?> list) {
			List<String> strList = list.stream().map(String::valueOf).toList();

			placeholder.add("{flag-description}", String.join("\n", strList));
		}

		placeholder.add("{state}", Formatter.getFlagState(value));
		placeholder.add("{flag-allowed}",
				Formatter.getBoolean(!Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flag)));

		ButtonData data = getButtonData(17);
		String type = Resources.<LanguageFile>get(ResourceType.Language).getString("flags-info." + flag + ".type");

		if (type != null && type.startsWith("PLAYERHEAD-")) {
			String texture = type.split("-")[1];

			return ItemUtils.getPlayerHead(data.getName(), data.getLore(), texture, placeholder);
		} else {
			return ItemUtils.getItem(data.getName(), data.getLore(),
					Material.getMaterial(type == null ? "BARRIER" : type), placeholder);
		}
	}

	public static ItemStack getBackButton() {
		return ItemUtils.getItem(getButtonData(0));
	}

	public static ItemStack getPreviousPageButton() {
		return ItemUtils.getItem(getButtonData(1));
	}

	public static ItemStack getNextPageButton() {
		return ItemUtils.getItem(getButtonData(2));
	}

	public static ItemStack getEmptySlot() {
		return ItemUtils.getItem(getButtonData(3));
	}

	public static class ButtonData {
		public final String name;
		public final List<String> lore;
		public final Material type;
		public String originalType;

		public ButtonData(String name, List<String> lore, String type) {
			this.name = name;
			this.lore = lore;
			this.type = Material.getMaterial(type);
			this.originalType = type;
		}

		public String getName() {
			return name;
		}

		public List<String> getLore() {
			return lore;
		}

		public Material getType() {
			return type == null ? Material.BARRIER : type;
		}

		public String getOriginalType() {
			return originalType;
		}
	}
}

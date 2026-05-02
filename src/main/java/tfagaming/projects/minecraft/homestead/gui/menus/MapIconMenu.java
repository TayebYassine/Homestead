package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.MapIcon;

import java.util.ArrayList;
import java.util.List;

public final class MapIconMenu {
    private final List<String> icons;

    public MapIconMenu(Player player, Region region) {
        this.icons = MapIcon.getAllIcons();

        PaginationMenu.builder(MenuUtility.getTitle(30).replace("{region}", region.getName()), 9 * 4)
                .nextPageItem(MenuUtility.getNextPageButton())
                .prevPageItem(MenuUtility.getPreviousPageButton())
                .items(getItems(player, region))
                .fillEmptySlots()
                .goBack((_player, event) -> new MiscellaneousSettings(player, region))
                .onClick((_player, context) -> handleMapIconClick(player, region, context))
                .build()
                .open(player);
    }

    private void handleMapIconClick(Player player, Region region, PaginationMenu.ClickContext context) {
        if (context.getIndex() >= icons.size()) return;

        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        boolean isEnabled = Resources.<ConfigFile>get(ResourceType.Config).getBoolean("dynamic-maps.icons.enabled");

        if (!isEnabled) {
            Messages.send(player, 105);
            player.closeInventory();
            return;
        }

        if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
            Messages.send(player, 159);
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE)) {
            Cooldown.sendCooldownMessage(player);
            return;
        }

        final String oldIcon = region.getMapIcon();

        Cooldown.startCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE);

        String icon = icons.get(context.getIndex());
        region.setMapIcon(icon);

        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

        Messages.send(player, 20, new Placeholder()
                .add("{oldicon}", oldIcon == null ? "None" : oldIcon)
                .add("{newicon}", icon)
        );

        Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region));
    }

    private List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();
        String currentIcon = region.getMapIcon();

        for (String icon : icons) {
            items.add(createIconItem(icon, icon.equals(currentIcon)));
        }

        return items;
    }

    private ItemStack createIconItem(String icon, boolean isSelected) {
        ItemStack item = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ColorTranslator.translate("&e" + icon));

            if (isSelected) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}
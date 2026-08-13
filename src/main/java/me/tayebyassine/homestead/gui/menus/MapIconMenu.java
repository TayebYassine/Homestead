package me.tayebyassine.homestead.gui.menus;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import me.tayebyassine.homestead.tools.java.Placeholder;
import me.tayebyassine.homestead.tools.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;
import me.tayebyassine.homestead.tools.minecraft.menus.MenuUtility;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerUtility;
import me.tayebyassine.homestead.tools.minecraft.plugins.MapIcon;

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

        if (!Resources.<ConfigFile>get(ResourceType.Config).getBoolean("dynamic-maps.icons.enabled")) {
            player.closeInventory();
            return;
        }

        if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
            Messages.send(player, "common.no_permission");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE)) {
            Cooldown.sendCooldownMessage(player);
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        Cooldown.startCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE);

        String icon = icons.get(context.getIndex());
        region.setMapIcon(icon);

        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

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
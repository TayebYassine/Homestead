package me.tayebyassine.homestead.integrations;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import me.tayebyassine.homestead.tools.java.Placeholder;
import me.tayebyassine.homestead.tools.minecraft.items.ItemUtility;
import me.tayebyassine.homestead.tools.minecraft.plugins.IntegrationUtility;

import java.util.List;

public class CraftEngine {

    public static ItemStack getCEItem(String itemId, String displayname, List<String> lore, Placeholder placeholder) {
        if (isAvailable()) {
            BukkitItemDefinition definition = CraftEngineItems.byId(itemId);

            ItemStack item = definition!= null? definition.buildBukkitItem().clone() : new ItemStack(Material.BARRIER);

            return ItemUtility.applyMetadata(item, displayname, lore, placeholder);
        }
        return ItemUtility.applyMetadata(new ItemStack(Material.BARRIER), displayname, lore, placeholder);
    }

    public static boolean isAvailable() {
        try {
            Class.forName("net.momirealms.craftengine.bukkit.api.CraftEngineItems");

            return IntegrationUtility.isEnabled(IntegrationUtility.Integration.CRAFTENGINE);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

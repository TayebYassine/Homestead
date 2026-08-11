package tfagaming.projects.minecraft.homestead.integrations;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.items.ItemUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.IntegrationUtility;

import java.util.List;

public class ItemsAdder {

    public static ItemStack getIAItem(String itemId, String displayname, List<String> lore, Placeholder placeholder) {
        if (isAvailable()) {
            CustomStack stack = CustomStack.getInstance(itemId.toLowerCase());

            ItemStack item = stack!= null? stack.getItemStack().clone() : new ItemStack(Material.BARRIER);

            return ItemUtility.applyMetadata(item, displayname, lore, placeholder);
        }
        return ItemUtility.applyMetadata(new ItemStack(Material.BARRIER), displayname, lore, placeholder);
    }

    public static boolean isAvailable() {
        try {
            Class.forName("dev.lone.itemsadder.api.CustomStack");

            return IntegrationUtility.isEnabled(IntegrationUtility.Integration.ITEMSADDER);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

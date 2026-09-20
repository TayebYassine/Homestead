package me.tayebyassine.homestead.gui.helpers;

import me.tayebyassine.homestead.integrations.CraftEngine;
import me.tayebyassine.homestead.integrations.ItemsAdder;
import me.tayebyassine.homestead.integrations.NexoMC;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.items.ItemUtility;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

/**
 * Resolves {@link ButtonData} into concrete {@link ItemStack} instances.
 *
 * <p>Handles material type resolution including special prefixes:</p>
 * <ul>
 *     <li>{@code PLAYERHEAD-<texture>} – player head with custom texture</li>
 *     <li>{@code PLAYERHEAD-this} – player head using the target player's skin</li>
 *     <li>{@code IA-<id>} – ItemsAdder custom item</li>
 *     <li>{@code NEXOMC-<id>} / {@code NEXO-<id>} – Nexo custom item</li>
 *     <li>{@code CE-<id>} – CraftEngine custom item</li>
 * </ul>
 *
 * <p>Dynamic types like {@code CUSTOM::GETBYWORLD} are resolved externally
 * by callers before invoking {@link #resolveItem}.</p>
 */
public final class MenuItems {
    private MenuItems() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Resolves a {@link ButtonData} into an {@link ItemStack}.
     *
     * @param data         the button data to resolve
     * @param typeOverride an override for the material type string, or {@code null} to use the data's type
     * @param placeholder  the placeholder values to apply to name and lore
     * @param player       the player for {@code PLAYERHEAD-this} resolution, or {@code null}
     * @return the constructed item stack
     */
    public static ItemStack resolveItem(ButtonData data, String typeOverride,
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

        if (type.startsWith("IA-")) {
            String itemId = type.split("-", 2)[1];
            return ItemsAdder.getIAItem(itemId, data.getName(), data.getLore(), placeholder);
        }

        if (type.startsWith("NEXOMC-") || type.startsWith("NEXO-")) {
            String itemId = type.split("-", 2)[1];
            return NexoMC.getNexoItem(itemId, data.getName(), data.getLore(), placeholder);
        }

        if (type.startsWith("CE-")) {
            String itemId = type.split("-", 2)[1];
            return CraftEngine.getCEItem(itemId, data.getName(), data.getLore(), placeholder);
        }

        Material material = Material.getMaterial(type);
        return ItemUtility.getItem(data.getName(), data.getLore(),
                material != null ? material : Material.BARRIER, placeholder);
    }
}

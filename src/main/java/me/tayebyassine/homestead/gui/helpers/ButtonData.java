package me.tayebyassine.homestead.gui.helpers;

import org.bukkit.Material;

import java.util.List;

/**
 * Holds the raw data for a single menu button: display name, lore lines, and item type.
 *
 * <p>Instances are created by {@link MenuButtons} and consumed by {@link MenuItems} to
 * produce the final {@link org.bukkit.inventory.ItemStack}.</p>
 */
public final class ButtonData {
    private final String name;
    private final List<String> lore;
    private Material type;
    private String originalType;

    /**
     * Creates a new button data instance.
     *
     * @param name the display name
     * @param lore the lore lines
     * @param type the raw material type string (e.g. {@code DIAMOND}, {@code PLAYERHEAD-...})
     */
    public ButtonData(String name, List<String> lore, String type) {
        this.name = name;
        this.lore = lore;
        this.originalType = type;
        this.type = Material.getMaterial(type);
    }

    /**
     * Returns the display name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the lore lines.
     */
    public List<String> getLore() {
        return lore;
    }

    /**
     * Returns the raw type string as defined in the YAML.
     */
    public String getOriginalType() {
        return originalType;
    }

    /**
     * Overrides the original type string and re-resolves the {@link Material}.
     *
     * @param type the new raw material type string
     */
    public void setOriginalType(String type) {
        this.originalType = type;
        this.type = Material.getMaterial(type);
    }

    /**
     * Returns the resolved {@link Material}, or {@link Material#BARRIER} if unresolvable.
     */
    public Material getType() {
        return type != null ? type : Material.BARRIER;
    }
}

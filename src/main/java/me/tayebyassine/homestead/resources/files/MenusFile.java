package me.tayebyassine.homestead.resources.files;

import me.tayebyassine.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Typed accessor for the menus YAML configuration file.
 */
public final class MenusFile extends ResourceFile {
    public MenusFile(File file) throws FileNotFoundException {
        super(file);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path) {
        return (T) getRaw(path);
    }

    /**
     * Returns the title for the given menu key.
     *
     * @param menuKey the menu key defined in the {@code menus} section
     * @return the title string, or the menuKey itself as a fallback
     */
    public String getMenuTitle(String menuKey) {
        return getString("menus." + menuKey + ".title", menuKey);
    }

    /**
     * Returns the display name for a per-menu button.
     *
     * @param menuKey  the menu configuration key
     * @param buttonId the button's local integer ID
     * @return the button name, or {@code "UNDEFINED"} if not found
     */
    public String getMenuButtonName(String menuKey, int buttonId) {
        return getString("menus." + menuKey + ".buttons." + buttonId + ".name", "UNDEFINED");
    }

    /**
     * Returns the lore lines for a per-menu button.
     *
     * @param menuKey  the menu configuration key
     * @param buttonId the button's local integer ID
     * @return the lore lines, or an empty list if not found
     */
    public List<String> getMenuButtonLore(String menuKey, int buttonId) {
        return getStringList("menus." + menuKey + ".buttons." + buttonId + ".lore");
    }

    /**
     * Returns the material type string for a per-menu button.
     *
     * @param menuKey  the menu configuration key
     * @param buttonId the button's local integer ID
     * @return the raw material type string (e.g. {@code DIAMOND}, {@code PLAYERHEAD-...}), or {@code BARRIER} if not found
     */
    public String getMenuButtonType(String menuKey, int buttonId) {
        return getString("menus." + menuKey + ".buttons." + buttonId + ".type", "BARRIER");
    }

    /**
     * Returns the display name for a named default button (e.g. "back", "prev-page").
     *
     * @param name the named button key
     * @return the button name, or {@code "UNDEFINED"} if not found
     */
    public String getDefaultButtonName(String name) {
        return getString("defaults." + name + ".name", "UNDEFINED");
    }

    /**
     * Returns the lore lines for a named default button.
     *
     * @param name the named button key
     * @return the lore lines, or an empty list if not found
     */
    public List<String> getDefaultButtonLore(String name) {
        return getStringList("defaults." + name + ".lore");
    }

    /**
     * Returns the material type string for a named default button.
     *
     * @param name the named button key
     * @return the raw material type string, or {@code "BARRIER"} if not found
     */
    public String getDefaultButtonType(String name) {
        return getString("defaults." + name + ".type", "BARRIER");
    }

    /**
     * Checks whether a menu section exists for the given key.
     *
     * @param menuKey the menu configuration key
     * @return {@code true} if the section exists
     */
    public boolean hasMenu(String menuKey) {
        return getConfig().isConfigurationSection("menus." + menuKey);
    }

    /**
     * Returns all menu keys defined in the {@code menus} section.
     *
     * @return list of menu keys, or an empty list if no menus are defined
     */
    public List<String> getMenuKeys() {
        if (!getConfig().isConfigurationSection("menus")) {
            return Collections.emptyList();
        }

        return new ArrayList<>(
                getConfig().getConfigurationSection("menus").getKeys(false));
    }
}

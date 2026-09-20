package me.tayebyassine.homestead.gui.helpers;

import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.FlagsFile;
import me.tayebyassine.homestead.resources.files.LanguageFile;
import me.tayebyassine.homestead.resources.files.MenusFile;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides access to menu button data and constructs {@link ItemStack} instances from
 * the YAML configuration.
 */
public final class MenuButtons {
    private MenuButtons() {
    }

    /**
     * Retrieves raw button data for a per-menu button.
     *
     * @param menuKey  the menu configuration key
     * @param buttonId the button's local integer ID within that menu
     * @return the button data
     */
    public static ButtonData getButtonData(String menuKey, int buttonId) {
        MenusFile menus = Resources.get(ResourceType.Menus);
        String name = menus.getMenuButtonName(menuKey, buttonId);
        List<String> lore = menus.getMenuButtonLore(menuKey, buttonId);
        String type = menus.getMenuButtonType(menuKey, buttonId);

        if (name == null) name = "UNDEFINED";
        if (lore == null) lore = new ArrayList<>();
        if (type == null) type = "BARRIER";

        return new ButtonData(name, lore, type);
    }

    /**
     * Retrieves raw button data for a default button (e.g. "back", "prev-page").
     *
     * @param name the default button key (e.g. {@code "back"})
     * @return the button data
     */
    public static ButtonData getDefaultButtonData(String name) {
        MenusFile menus = Resources.get(ResourceType.Menus);
        String btnName = menus.getDefaultButtonName(name);
        List<String> lore = menus.getDefaultButtonLore(name);
        String type = menus.getDefaultButtonType(name);

        if (btnName == null) btnName = "UNDEFINED";
        if (lore == null) lore = new ArrayList<>();
        if (type == null) type = "BARRIER";

        return new ButtonData(btnName, lore, type);
    }

    /**
     * Builds an {@link ItemStack} from raw button data.
     *
     * @param data the button data
     * @return the constructed item stack
     */
    public static ItemStack getButton(ButtonData data) {
        return MenuItems.resolveItem(data, data.getOriginalType(), new Placeholder(), null);
    }

    /**
     * Builds an {@link ItemStack} from raw button data with placeholder substitution.
     *
     * @param data        the button data
     * @param placeholder the placeholders to apply
     * @return the constructed item stack
     */
    public static ItemStack getButton(ButtonData data, Placeholder placeholder) {
        return MenuItems.resolveItem(data, data.getOriginalType(), placeholder, null);
    }

    /**
     * Builds an {@link ItemStack} from raw button data with placeholder substitution.
     *
     * @param data        the button data
     * @param placeholder the placeholders to apply
     * @param playerHead  the player for {@code PLAYERHEAD-this} type
     * @return the constructed item stack
     */
    public static ItemStack getButton(ButtonData data, Placeholder placeholder, OfflinePlayer playerHead) {
        return MenuItems.resolveItem(data, data.getOriginalType(), placeholder, playerHead);
    }

    /**
     * Builds an {@link ItemStack} from a per-menu button.
     *
     * @param menuKey  the menu configuration key
     * @param buttonId the button's local integer ID
     * @return the constructed item stack
     */
    public static ItemStack getButton(String menuKey, int buttonId) {
        return getButton(menuKey, buttonId, new Placeholder(), null);
    }

    /**
     * Builds an {@link ItemStack} from a per-menu button with placeholders.
     *
     * @param menuKey     the menu configuration key
     * @param buttonId    the button's local integer ID
     * @param placeholder the placeholders to apply
     * @return the constructed item stack
     */
    public static ItemStack getButton(String menuKey, int buttonId, Placeholder placeholder) {
        return MenuItems.resolveItem(getButtonData(menuKey, buttonId), null, placeholder, null);
    }

    /**
     * Builds an {@link ItemStack} from a per-menu button with placeholders and player head.
     *
     * @param menuKey     the menu configuration key
     * @param buttonId    the button's local integer ID
     * @param placeholder the placeholders to apply
     * @param playerHead  the player for {@code PLAYERHEAD-this} type
     * @return the constructed item stack
     */
    public static ItemStack getButton(String menuKey, int buttonId, Placeholder placeholder, OfflinePlayer playerHead) {
        return MenuItems.resolveItem(getButtonData(menuKey, buttonId), null, placeholder, playerHead);
    }

    /**
     * Builds an {@link ItemStack} from a per-menu button with a player head.
     *
     * @param menuKey    the menu configuration key
     * @param buttonId   the button's local integer ID
     * @param playerHead the player for {@code PLAYERHEAD-this} type
     * @return the constructed item stack
     */
    public static ItemStack getButton(String menuKey, int buttonId, OfflinePlayer playerHead) {
        return getButton(menuKey, buttonId, new Placeholder(), playerHead);
    }

    /**
     * Builds an {@link ItemStack} from a default button (e.g. "back", "prev-page").
     *
     * @param name the button key
     * @return the constructed item stack
     */
    public static ItemStack getDefaultButton(String name) {
        return MenuItems.resolveItem(getDefaultButtonData(name), null, new Placeholder(), null);
    }

    /**
     * Builds a flag toggle button using a per-menu flag button definition.
     *
     * @param menuKey  the menu configuration key
     * @param buttonId the button's local integer ID
     * @param flag     the flag identifier
     * @param value    the current flag value
     * @return the constructed item stack
     */
    public static ItemStack getFlagButton(String menuKey, int buttonId, String flag, boolean value) {
        ButtonData data = getButtonData(menuKey, buttonId);

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
        placeholder.add("{flag-allowed}", Formatter.getBoolean(!Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flag)));

        String type = Resources.<LanguageFile>get(ResourceType.Language).getString("flags-info." + flag + ".type");

        return MenuItems.resolveItem(data, type, placeholder, null);
    }

    /**
     * Builds the back button from default.
     *
     * @return the back button item stack
     */
    public static ItemStack getBackButton() {
        return getDefaultButton("back");
    }

    /**
     * Builds the previous-page button from default.
     *
     * @return the previous-page button item stack
     */
    public static ItemStack getPreviousPageButton() {
        return getDefaultButton("prev-page");
    }

    /**
     * Builds the next-page button from default.
     *
     * @return the next-page button item stack
     */
    public static ItemStack getNextPageButton() {
        return getDefaultButton("next-page");
    }

    /**
     * Builds the empty-slot filler from default.
     *
     * @return the empty-slot item stack
     */
    public static ItemStack getEmptySlot() {
        return getDefaultButton("empty-slot");
    }

    /**
     * Wraps a message to a fixed line width, inserting newlines at word or color-code
     * boundaries to avoid breaking formatting sequences.
     *
     * @param message the raw message string
     * @return the wrapped message
     */
    public static String wrapMessage(String message) {
        message = ColorTranslator.preserve(message);

        int wrapLength = 40;
        StringBuilder sb = new StringBuilder();
        int lineLength = 0;

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            if (c == '\n') {
                sb.append(c);
                lineLength = 0;
                continue;
            }

            if (lineLength >= wrapLength && (c == ' ' || c == '&' || c == '\u00A7')) {
                sb.append('\n');
                lineLength = 0;
                if (c == ' ') continue;
            }

            sb.append(c);
            lineLength++;

            if (c == '&' || c == '\u00A7') {
                lineLength--;
            }
        }

        return sb.toString();
    }
}

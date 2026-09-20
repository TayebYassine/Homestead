package me.tayebyassine.homestead.gui.helpers;

import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.MenusFile;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;

/**
 * Resolves menu titles from the YAML configuration.
 */
public final class MenuTitles {
    private MenuTitles() {
    }

    /**
     * Resolves a menu title by its configuration key.
     *
     * @param menuKey the menu key defined in the {@code menus} section of the YAML
     * @return the resolved title string, or the menuKey itself as a fallback
     */
    public static String getTitle(String menuKey) {
        return Resources.<MenusFile>get(ResourceType.Menus).getMenuTitle(menuKey);
    }

    public static String getTitle(String menuKey, Placeholder placeholder) {
        return Formatter.applyPlaceholders(getTitle(menuKey), placeholder);
    }
}

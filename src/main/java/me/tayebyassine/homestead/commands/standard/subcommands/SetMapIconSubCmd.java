package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.gui.menus.MapIconMenu;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.plugins.MapIcon;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs setmapicon}) that changes the map icon of the current region,
 * or opens the icon menu when no icon is specified.
 */
public final class SetMapIconSubCmd extends SubCommandBuilder {

    public SetMapIconSubCmd() {
        super("setmapicon");
        setRegionPermission("homestead.actions.regions.update.map_icon");
        setUsage("/hs setmapicon [icon]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.setmapicon.0");
            return true;
        }

        if (!(Resources.<ConfigFile>get(ResourceType.Config).getBoolean("dynamic-maps.enabled")
                && Resources.<ConfigFile>get(ResourceType.Config).getBoolean("dynamic-maps.icons.enabled"))) {
            Messages.send(player, "commands.setmapicon.1");
            return true;
        }

        if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE)) {
            Cooldown.sendCooldownMessage(player);
            return true;
        }

        if (args.length < 1) {
            new MapIconMenu(player, region);

            return true;
        }

        String iconInput = args[0];

        if (iconInput.equals("Default")) {
            region.setMapIcon(iconInput);

            Messages.send(player, "commands.setmapicon.4");

            return true;
        }

        if (!MapIcon.isValidIcon(iconInput)) {
            Messages.send(player, "commands.setmapicon.2");
            return true;
        }

        if (region.getMapIcon() != null && region.getMapIcon().equals(iconInput)) {
            Messages.send(player, "commands.setmapicon.3");
            return true;
        }

        Cooldown.startCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE);

        region.setMapIcon(iconInput);

        Messages.send(player, "commands.setmapicon.5");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return new ArrayList<>();
        }

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(MapIcon.getAllIcons());
            suggestions.add("Default");
        }

        return suggestions;
    }
}




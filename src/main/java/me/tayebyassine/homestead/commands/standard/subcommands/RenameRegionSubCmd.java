package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RegionNameUpdateEvent;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.StringUtils;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs rename}) that renames the current region.
 */
public final class RenameRegionSubCmd extends SubCommandBuilder {

    public RenameRegionSubCmd() {
        super("rename");
        setRegionPermission("homestead.actions.regions.update.name");
        setUsage("/hs rename [new-name]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.rename.0");
            return true;
        }

        if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE)) {
            Cooldown.sendCooldownMessage(player);
            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.rename.1");
            return true;
        }

        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.RENAME_REGION, true)) {
            return true;
        }

        String regionName = args[0];

        if (!StringUtils.isValidRegionName(regionName)) {
            Messages.send(player, "commands.rename.3");
            return true;
        }

        if (regionName.equalsIgnoreCase(region.getName())) {
            Messages.send(player, "commands.rename.4");
            return true;
        }

        if (RegionManager.isNameUsed(regionName)) {
            Messages.send(player, "commands.rename.5");
            return true;
        }

        if (ColorTranslator.containsMiniMessageTag(regionName)) {
            Messages.send(player, "commands.rename.6");
            return true;
        }

        final String oldName = region.getName();

        Cooldown.startCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE);

        RegionManager.renameRegion(region, regionName);

        LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_NAME, regionName);

        Messages.send(player, "commands.rename.7", oldName, regionName);

        Homestead.callEvent(new RegionNameUpdateEvent(region, oldName, regionName));

        return true;
    }
}





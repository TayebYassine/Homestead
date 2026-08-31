package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RegionDisplaynameUpdateEvent;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.StringUtils;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Sub-command ({@code /hs setdisplayname}) that sets the display name of the current region.
 */
public final class SetDisplayNameSubCmd extends SubCommandBuilder {

    public SetDisplayNameSubCmd() {
        super("setdisplayname");
        setRegionPermission("homestead.actions.regions.update.displayname");
        setUsage("/hs setdisplayname [name]");
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
            Messages.send(player, "commands.setdisplayname.0");
            return true;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.setdisplayname.1");
            return true;
        }

        if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE)) {
            Cooldown.sendCooldownMessage(player);
            return true;
        }

        String regionDisplayName = String.join(" ", Arrays.asList(args));

        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.RENAME_REGION, true)) {
            return true;
        }

        if (!StringUtils.isValidRegionDisplayName(regionDisplayName)) {
            Messages.send(player, "commands.setdisplayname.3");
            return true;
        }

        final String oldDisplayName = region.getDisplayName();

        if (oldDisplayName != null && oldDisplayName.equals(regionDisplayName)) {
            Messages.send(player, "commands.setdisplayname.4");
            return true;
        }

        if (ColorTranslator.containsMiniMessageTag(regionDisplayName)) {
            Messages.send(player, "commands.setdisplayname.5");
            return true;
        }

        Cooldown.startCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE);

        region.setDisplayName(regionDisplayName);

        LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_DISPLAYNAME, regionDisplayName);

        Messages.send(player, "commands.setdisplayname.6",
                oldDisplayName == null ? Formatter.getNone() : oldDisplayName, regionDisplayName);

        Homestead.callEvent(new RegionDisplaynameUpdateEvent(region, oldDisplayName, regionDisplayName));

        return true;
    }
}





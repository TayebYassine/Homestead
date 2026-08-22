package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RegionDescriptionUpdateEvent;
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
 * Sub-command ({@code /hs setdescription}) that sets the description of the current region.
 */
public final class SetDescriptionSubCmd extends SubCommandBuilder {

    public SetDescriptionSubCmd() {
        super("setdescription");
        setRegionPermission("homestead.actions.regions.update.description");
        setUsage("/hs setdescription [description]");
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
            Messages.send(player, "commands.setdescription.0");
            return true;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.setdescription.1");
            return true;
        }

        if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DESCRIPTION_CHANGE)) {
            Cooldown.sendCooldownMessage(player);
            return true;
        }

        String description = String.join(" ", Arrays.asList(args));

        if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                ControlFlag.SET_DESCRIPTION.getBitmask())) {
            return true;
        }

        if (!StringUtils.isValidRegionDescription(description)) {
            Messages.send(player, "commands.setdescription.3");
            return true;
        }

        final String oldDescription = region.getDescription();

        if (oldDescription != null && oldDescription.equals(description)) {
            Messages.send(player, "commands.setdescription.4");
            return true;
        }

        if (ColorTranslator.containsMiniMessageTag(description)) {
            Messages.send(player, "commands.setdescription.5");
            return true;
        }

        Cooldown.startCooldown(player, Cooldown.Type.REGION_DESCRIPTION_CHANGE);

        region.setDescription(description);

        LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_DESCRIPTION, description);

        Messages.send(player, "commands.setdescription.6");

        Homestead.callEvent(new RegionDescriptionUpdateEvent(region,
                oldDescription == null ? Formatter.getNone() : oldDescription, description));

        return true;
    }
}





package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.AutoClaimSession;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs auto}) that toggles automatic chunk claiming while walking.
 */
public final class AutoSubCmd extends SubCommandBuilder {

    public AutoSubCmd() {
        super("auto");
        setRegionPermission(
                "homestead.actions.regions.create",
                "homestead.actions.regions.chunks.claim"
        );
        setUsage("/hs auto");
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
            Messages.send(player, "commands.auto.2");
            return true;
        }

        if (AutoClaimSession.hasSession(player)) {
            AutoClaimSession.removeSession(player);

            Messages.send(player, "commands.auto.1");
        } else {
            AutoClaimSession.newSession(player);

            Messages.send(player, "commands.auto.0", region.getName());
        }

        return true;
    }
}




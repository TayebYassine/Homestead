package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.ClaimFlySession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs fly}) that toggles flight while inside the player's region.
 */
public final class FlySubCmd extends SubCommandBuilder {

    public FlySubCmd() {
        super("fly");
        setRegionPermission("homestead.actions.regions.fly");
        setUsage("/hs fly");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        Chunk chunk = player.getLocation().getChunk();
        Region region = ChunkManager.getRegionOwnsTheChunk(chunk);

        if (region == null
                || (!PlayerUtility.isOperator(player)
                && !(region.isOwner(player) || MemberManager.isMemberOfRegion(region, player)))) {
            Messages.send(player, "commands.fly.0");
            return true;
        }

        if (ClaimFlySession.hasSession(player)) {
            ClaimFlySession.removeSession(player);

            player.setAllowFlight(false);
            player.setFlying(false);

            Messages.send(player, "commands.fly.2");
        } else {
            ClaimFlySession.newSession(player);

            player.setAllowFlight(true);
            player.setFlying(true);

            Messages.send(player, "commands.fly.1");
        }

        return true;
    }
}




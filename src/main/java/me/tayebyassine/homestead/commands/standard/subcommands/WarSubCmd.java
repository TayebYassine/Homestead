package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.WorldFlags;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.WarManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.War;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.NumberUtils;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sub-command ({@code /hs war}) that manages wars between regions:
 * declare, surrender, or inspect the current war.
 */
public final class WarSubCmd extends SubCommandBuilder {

    public WarSubCmd() {
        super("war");
        setRegionPermission("homestead.actions.regions.war");
        setUsage("/hs war [action] (params)");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (!Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("wars.enabled")) {
            Messages.send(player, "commands.war.1");
            return true;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.war.2", getUsage());
            return true;
        }

        if (!Homestead.VAULT.isEconomyReady()) {
            Messages.send(player, "commands.war.3");

            Logger.warning(Logger.PredefinedMessage.ECONOMY_INTEGRATION_DISABLED);

            return true;
        }

        return switch (args[0]) {
            case "declare" -> declareWar(player, args);
            case "surrender" -> surrender(player);
            case "info" -> warInfo(player);
            default -> true;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return new ArrayList<>();
        }

        List<String> suggestions = new ArrayList<>();

        if (args.length == 2 && args[0].equalsIgnoreCase("declare")) {
            suggestions.addAll(RegionManager.getAll().stream().map(Region::getName).toList());
        }

        return suggestions;
    }

    private boolean declareWar(Player player, String[] args) {
        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.war.4");
            return true;
        }

        if (WarManager.isRegionInWar(region.getUniqueId())) {
            Messages.send(player, "commands.war.5");
            return true;
        }

        if (args.length < 4) {
            Messages.send(player, "commands.war.2", "/hs war declare [target] [prize] (war name)");
            return true;
        }

        Region targetRegion = RegionManager.findRegion(args[1]);

        if (targetRegion == null) {
            Messages.send(player, "commands.war.6");
            return true;
        }

        if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
            Messages.send(player, "commands.war.7");
            return true;
        }

        if (region.getUniqueId() == targetRegion.getUniqueId()
                || region.isOwner(targetRegion.getOwnerId())) {
            Messages.send(player, "commands.war.8");
            return true;
        }

        if (!(region.isWorldFlagSet(WorldFlags.WARS) && targetRegion.isWorldFlagSet(WorldFlags.WARS))) {
            Messages.send(player, "commands.war.9");
            return true;
        }

        if (WarManager.isRegionInWar(targetRegion.getUniqueId())) {
            Messages.send(player, "commands.war.10");
            return true;
        }

        String prizeInput = args[2];

        if (!NumberUtils.isValidDouble(prizeInput) || Double.parseDouble(prizeInput) > Integer.MAX_VALUE) {
            Messages.send(player, "commands.war.11");
            return true;
        }

        double prize = Double.parseDouble(prizeInput);

        double minPrize = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("wars.min-prize");
        double maxPrize = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("wars.max-prize");

        if (prize < minPrize || prize > maxPrize) {
            Messages.send(player, "commands.war.12", Formatter.getBalance(minPrize), Formatter.getBalance(maxPrize));
            return true;
        }

        if (!(targetRegion.getBank() >= prize && region.getBank() >= prize)) {
            Messages.send(player, "commands.war.13");
            return true;
        }

        String name = String.join(" ", Arrays.asList(args).subList(3, args.length));

        if (name.isEmpty()) {
            name = "War";
        }

        if (name.length() > 128 || ColorTranslator.containsMiniMessageTag(name)) {
            Messages.send(player, "commands.war.14");
            return true;
        }

        War war = WarManager.declareWar(name, prize, region, targetRegion);

        WarManager.broadcastDeclarationOfWar(war);

        return true;
    }

    private boolean surrender(Player player) {
        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.war.4");
            return true;
        }

        if (!WarManager.isRegionInWar(region.getUniqueId())) {
            Messages.send(player, "commands.war.15");
            return true;
        }

        War war = WarManager.findWarByRegion(region.getUniqueId());

        final List<OfflinePlayer> warMembers = List.copyOf(WarManager.getMembersOfWar(war.getUniqueId()));

        war = WarManager.removeRegionFromWar(region.getUniqueId());

        if (war != null) {
            Region winner = war.getWinner();

            if (winner != null) {
                double prize = war.getPrize();

                region.withdrawBank(prize);
                winner.depositBank(prize);

                Player owner = getOnlineOwner(winner);

                if (owner != null) {
                    Messages.send(owner, "common.war_player_winner");

                    Cooldown.startCooldown(owner, Cooldown.Type.WAR_FLAG_DISABLED);
                }
            }

            Player owner = getOnlineOwner(region);

            if (owner != null) {
                Cooldown.startCooldown(owner, Cooldown.Type.WAR_FLAG_DISABLED);
            }

            WarManager.tellPlayersWarEnded(warMembers, winner);

            WarManager.endWar(war.getUniqueId());
        }

        Messages.send(player, "commands.war.16");

        return true;
    }

    private boolean warInfo(Player player) {
        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.war.4");
            return true;
        }

        War war = WarManager.findWarByRegion(region.getUniqueId());

        if (war == null) {
            Messages.send(player, "commands.war.15");
            return true;
        }

        Messages.send(player, "commands.war.17", Formatter.getRegionsOfWar(war), Formatter.getBalance(war.getPrize()));

        return true;
    }

    private Player getOnlineOwner(Region region) {
        OfflinePlayer offlineOwner = region.getOwner();

        return offlineOwner != null && offlineOwner.isOnline() ? offlineOwner.getPlayer() : null;
    }
}




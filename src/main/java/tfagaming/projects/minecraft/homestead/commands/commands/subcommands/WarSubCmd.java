package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.WarsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.*;

public class WarSubCmd extends SubCommandBuilder {
    public WarSubCmd() {
        super("war");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("homestead.region.war")) {
            PlayerUtils.sendMessage(player, 8);
            return true;
        }

        if (args.length < 2) {
            PlayerUtils.sendMessage(player, 0);
            return true;
        }

        if (!Homestead.vault.isEconomyReady()) {
            PlayerUtils.sendMessage(player, 69);
            Logger.warning("The player \"" + player.getName() + "\" (UUID: " + player.getUniqueId() + ") executed a command that requires economy implementation, but it's disabled.");
            Logger.warning("The execution has been ignored, you may resolve this issue by installing a plugin that implements economy on the server.");

            return true;
        }

        switch (args[1]) {
            case "declare": {
                Region region = TargetRegionSession.getRegion(player);

                if (region != null && WarsManager.isRegionInWar(region.getUniqueId())) {
                    PlayerUtils.sendMessage(player, 151);
                    return false;
                }

                if (args.length < 4) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                if (region == null) {
                    PlayerUtils.sendMessage(player, 4);
                    return false;
                }

                String targetRegionName = args[2];
                Region targetRegion = RegionsManager.findRegion(targetRegionName);

                if (targetRegion == null) {
                    PlayerUtils.sendMessage(player, 9);
                    return true;
                }

                if (!region.getOwnerId().equals(player.getUniqueId()) && region.isPlayerMember(player)) {
                    PlayerUtils.sendMessage(player, 149);
                    return false;
                }

                if (region.getUniqueId().equals(targetRegion.getUniqueId()) || region.getOwnerId().equals(targetRegion.getOwnerId())) {
                    PlayerUtils.sendMessage(player, 148);
                    return false;
                }

                if (WarsManager.isRegionInWar(targetRegion.getUniqueId())) {
                    PlayerUtils.sendMessage(player, 150);
                    return false;
                }

                String prizeInput = args[3];

                if ((!NumberUtils.isValidDouble(prizeInput))
                        || (NumberUtils.isValidDouble(prizeInput) && Double.parseDouble(prizeInput) > Integer.MAX_VALUE)) {
                    PlayerUtils.sendMessage(player, 146);
                    return true;
                }

                double prize = Double.parseDouble(prizeInput);

                if (targetRegion.getBank() < prize) {
                    PlayerUtils.sendMessage(player, 157);
                    return true;
                }

                List<String> nameList = Arrays.asList(args).subList(4, args.length);
                String name = String.join(" ", nameList);

                if (name.isEmpty()) name = "Unnamed War";

                if (name.length() > 512) {
                    PlayerUtils.sendMessage(player, 145);
                    return true;
                }

                War war = WarsManager.declareWar(name, prize, List.of(region, targetRegion));

                List<String> listString = Homestead.language.get("147");

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{regionplayer}", region.getName());
                replacements.put("{regiontarget}", targetRegion.getName());
                replacements.put("{prize}", Formatters.formatBalance(prize));

                List<OfflinePlayer> players = WarsManager.getMembersOfWar(war.getUniqueId());

                for (OfflinePlayer p : players) {
                    if (p.isOnline()) {
                        ((Player) p).playSound(p.getLocation(), Sound.EVENT_MOB_EFFECT_RAID_OMEN, SoundCategory.PLAYERS, 1f, 1f);

                        for (String string : listString) {
                            ((Player) p).sendMessage(ChatColorTranslator.translate(Formatters.replace(string, replacements)));
                        }
                    }
                }

                break;
            }

            case "surrender": {
                if (args.length < 2) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                Region region = TargetRegionSession.getRegion(player);

                if (region == null) {
                    PlayerUtils.sendMessage(player, 4);
                    return false;
                }

                if (!WarsManager.isRegionInWar(region.getUniqueId())) {
                    PlayerUtils.sendMessage(player, 152);
                    return true;
                }

                War war = WarsManager.surrenderRegionFromFirstWarFound(region.getUniqueId());

                if (war != null && war.getRegions().size() == 1) {
                    Region winner = war.getRegions().getFirst();

                    double prize = war.getPrize();

                    region.removeBalanceFromBank(prize);
                    winner.addBalanceToBank(prize);

                    if (winner.getOwner().isOnline()) {
                        PlayerUtils.sendMessage((Player) winner.getOwner(), 155);
                    }

                    WarsManager.endWar(war.getUniqueId());
                }

                PlayerUtils.sendMessage(player, 153);

                break;
            }

            case "info": {
                Region region = TargetRegionSession.getRegion(player);

                if (region == null) {
                    PlayerUtils.sendMessage(player, 4);
                    return false;
                }

                War war = WarsManager.findWarByRegionId(region.getUniqueId());

                if (!WarsManager.isRegionInWar(region.getUniqueId()) || war == null) {
                    PlayerUtils.sendMessage(player, 152);
                    return true;
                }

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{regions}", Formatters.getRegionsOfWar(war));
                replacements.put("{prize}", Formatters.formatBalance(war.prize));

                PlayerUtils.sendMessage(player, 154, replacements);
            }
        }

        return true;
    }
}

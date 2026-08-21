package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.PlayerJoinSubAreaEvent;
import me.tayebyassine.homestead.api.events.PlayerLeftSubAreaEvent;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.ControlFlags;
import me.tayebyassine.homestead.flags.FlagsCalculator;
import me.tayebyassine.homestead.flags.PlayerFlags;
import me.tayebyassine.homestead.gui.menus.SubAreasMenu;
import me.tayebyassine.homestead.listeners.SelectionToolListener;
import me.tayebyassine.homestead.listeners.SelectionToolListener.Selection;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionMember;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeBlock;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.StringUtils;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.chunks.ChunkUtility;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import me.tayebyassine.homestead.util.minecraft.subareas.SubAreaUtility;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs subareas}) that manages the sub-areas of the current region:
 * create, configure, and manage their players and flags.
 */
public final class SubAreasSubCmd extends SubCommandBuilder {

    public SubAreasSubCmd() {
        super("subareas");
        setRegionPermission();
        setUsage("/hs subareas [create|conf] [args]");
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
            Messages.send(player, "commands.subareas.0");
            return true;
        }

        if (args.length == 0) {
            new SubAreasMenu(player, region);
            return true;
        }

        if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                ControlFlags.MANAGE_SUBAREAS)) {
            return true;
        }

        return switch (args[0]) {
            case "create" -> executeCreate(player, region, args);
            case "conf" -> executeConfig(player, region, args);
            default -> {
                Messages.send(player, "commands.subareas.1", getUsage());
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return new ArrayList<>();
        }

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(List.of("create", "conf"));
        } else if (args.length == 2 && args[0].equals("conf")) {
            Region region = TargetRegionSession.getRegion(player);

            if (region != null) {
                suggestions.addAll(SubAreaManager.getSubAreasOfRegion(region.getUniqueId()).stream()
                        .map(SubArea::getName).toList());
            }
        } else if (args.length == 3 && args[0].equals("conf")) {
            suggestions.addAll(List.of("delete", "rename", "resize", "flags", "players"));
        } else if (args.length == 4 && args[0].equals("conf")) {
            if (args[2].equals("flags")) {
                suggestions.addAll(PlayerFlags.getFlags());
            } else if (args[2].equals("players")) {
                suggestions.addAll(List.of("add", "remove", "flags"));
            }
        } else if (args.length == 5 && args[0].equals("conf") && args[2].equals("players")) {
            if (args[3].equals("add") || args[3].equals("remove") || args[3].equals("flags")) {
                suggestions.addAll(getMemberNames(player));
            }
        } else if (args.length == 5 && args[0].equals("conf") && args[2].equals("flags")) {
            suggestions.addAll(List.of("allow", "deny"));
        } else if (args.length == 6 && args[0].equals("conf") && args[2].equals("players")
                && args[3].equals("flags")) {
            suggestions.addAll(PlayerFlags.getFlags());
        } else if (args.length == 7 && args[0].equals("conf") && args[2].equals("players")
                && args[3].equals("flags")) {
            suggestions.addAll(List.of("allow", "deny"));
        }

        return suggestions;
    }

    private boolean executeCreate(Player player, Region region, String[] args) {
        if (args.length < 2) {
            Messages.send(player, "commands.subareas.1", "/hs subareas create [name]");
            return true;
        }

        if (!player.hasPermission("homestead.actions.regions.subareas.create")) {
            Messages.send(player, "common.no_permission");
            return true;
        }

        Selection session = SelectionToolListener.getPlayerSession(player);

        if (session == null) {
            Messages.send(player, "commands.subareas.2");
            return true;
        }

        Block firstCorner = session.getFirstPosition();
        Block secondCorner = session.getSecondPosition();

        for (Chunk chunk : ChunkUtility.getChunksInArea(firstCorner, secondCorner)) {
            if (!ChunkManager.isChunkClaimedByRegion(region, chunk)) {
                Messages.send(player, "commands.subareas.3");
                return true;
            }
        }

        if (SubAreaUtility.isIntersectingOtherSubArea(region.getUniqueId(), new SeBlock(firstCorner),
                new SeBlock(secondCorner))) {
            Messages.send(player, "commands.subareas.4");
            return true;
        }

        String name = args[1];

        if (!StringUtils.isValidSubAreaName(name)) {
            Messages.send(player, "commands.subareas.5");
            return true;
        }

        if (SubAreaManager.isNameUsed(region.getUniqueId(), name)) {
            Messages.send(player, "commands.subareas.6");
            return true;
        }

        if (Limits.hasReachedLimit(null, region, Limits.LimitType.SUBAREAS_PER_REGION)) {
            Messages.send(player, "commands.subareas.7");
            return true;
        }

        int volume = SubAreaUtility.getVolume(firstCorner, secondCorner);
        int maxVolume = Limits.getRegionLimit(region, Limits.LimitType.MAX_SUBAREA_VOLUME);

        if (volume > maxVolume) {
            Messages.send(player, "commands.subareas.8", volume, maxVolume);
            return true;
        }

        SubAreaManager.createSubArea(region, name, firstCorner.getWorld(), firstCorner, secondCorner);

        SelectionToolListener.cancelPlayerSession(player);

        Messages.send(player, "commands.subareas.9", region.getName(), volume);

        LogManager.addLog(region, player, LogManager.PredefinedLog.CREATE_SUBAREA);

        return true;
    }

    private boolean executeConfig(Player player, Region region, String[] args) {
        if (args.length < 3) {
            Messages.send(player, "commands.subareas.1", "/hs subareas conf [subarea name] [action] (params)");
            return true;
        }

        String subAreaName = args[1];

        SubArea subArea = SubAreaManager.findSubArea(region.getUniqueId(), subAreaName);

        if (subArea == null) {
            Messages.send(player, "commands.subareas.10");
            return true;
        }

        return switch (args[2]) {
            case "delete" -> configDelete(player, region, subArea);
            case "rename" -> configRename(player, region, subArea, args);
            case "resize" -> configResize(player, region, subArea);
            case "flags" -> configFlags(player, region, subArea, args);
            case "players" -> configPlayers(player, region, subArea, args);
            default -> {
                Messages.send(player, "commands.subareas.1",
                        "/hs subareas conf [subarea name] [delete|rename|resize|flags|players] (params)");
                yield true;
            }
        };
    }

    private boolean configDelete(Player player, Region region, SubArea subArea) {
        if (!player.hasPermission("homestead.actions.regions.subareas.delete")) {
            Messages.send(player, "common.no_permission");
            return true;
        }

        SubAreaManager.deleteSubArea(subArea.getUniqueId());

        Messages.send(player, "commands.subareas.11", subArea.getName());

        LogManager.addLog(region, player, LogManager.PredefinedLog.DELETE_SUBAREA);

        return true;
    }

    private boolean configRename(Player player, Region region, SubArea subArea, String[] args) {
        if (!player.hasPermission("homestead.actions.regions.subareas.update.name")) {
            Messages.send(player, "common.no_permission");
            return true;
        }

        if (args.length < 4) {
            Messages.send(player, "commands.subareas.1", "/hs subareas conf [subarea name] rename [new name]");
            return true;
        }

        String newName = args[3];

        if (!StringUtils.isValidSubAreaName(newName)) {
            Messages.send(player, "commands.subareas.5");
            return true;
        }

        if (subArea.getName().equalsIgnoreCase(newName)) {
            Messages.send(player, "commands.subareas.12");
            return true;
        }

        if (SubAreaManager.isNameUsed(region.getUniqueId(), newName)) {
            Messages.send(player, "commands.subareas.6");
            return true;
        }

        if (ColorTranslator.containsMiniMessageTag(newName)) {
            Messages.send(player, "commands.subareas.13");
            return true;
        }

        final String oldName = subArea.getName();

        subArea.setName(newName);

        Messages.send(player, "commands.subareas.14", oldName, newName);

        return true;
    }

    private boolean configResize(Player player, Region region, SubArea subArea) {
        if (!player.hasPermission("homestead.actions.regions.subareas.resize")) {
            Messages.send(player, "common.no_permission");
            return true;
        }

        Selection session = SelectionToolListener.getPlayerSession(player);

        if (session == null) {
            Messages.send(player, "commands.subareas.2");
            return true;
        }

        Block firstCorner = session.getFirstPosition();
        Block secondCorner = session.getSecondPosition();

        for (Chunk chunk : ChunkUtility.getChunksInArea(firstCorner, secondCorner)) {
            if (!(ChunkManager.isChunkClaimedByRegion(region, chunk)
                    && firstCorner.getWorld().getUID().equals(subArea.getWorldId()))) {
                Messages.send(player, "commands.subareas.3");
                return true;
            }
        }

        SubArea intersectedSubArea = SubAreaUtility.getIntersectedSubArea(region.getUniqueId(),
                new SeBlock(firstCorner), new SeBlock(secondCorner));

        if (intersectedSubArea != null && intersectedSubArea.getUniqueId() != subArea.getUniqueId()) {
            Messages.send(player, "commands.subareas.4");
            return true;
        }

        subArea.setPoint1(firstCorner);
        subArea.setPoint2(secondCorner);

        Messages.send(player, "commands.subareas.15");

        return true;
    }

    private boolean configFlags(Player player, Region region, SubArea subArea, String[] args) {
        if (!player.hasPermission("homestead.actions.regions.subareas.update.flags.global")) {
            Messages.send(player, "common.no_permission");
            return true;
        }

        if (args.length < 4) {
            Messages.send(player, "commands.subareas.1", "/hs subareas conf [subarea name] flags [flag] (state)");
            return true;
        }

        String flagInput = args[3];

        if (!PlayerFlags.getFlags().contains(flagInput)) {
            Messages.send(player, "commands.subareas.16");
            return true;
        }

        long flags = subArea.getPlayerFlags();
        long flag = PlayerFlags.valueOf(flagInput);

        boolean denyState = resolveDenyState(FlagsCalculator.isFlagSet(flags, flag),
                args.length > 4 ? args[4] : null);

        subArea.setPlayerFlags(denyState ? FlagsCalculator.removeFlag(flags, flag)
                : FlagsCalculator.addFlag(flags, flag));

        Messages.send(player, "commands.subareas.17", flagInput, Formatter.getFlagState(!denyState), subArea.getName());

        LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE,
                flagInput, subArea.getName(), Formatter.getFlagState(!denyState));

        return true;
    }

    private boolean configPlayers(Player player, Region region, SubArea subArea, String[] args) {
        if (args.length < 4) {
            Messages.send(player, "commands.subareas.1",
                    "/hs subareas conf [subarea name] players [add|remove|flags] ...");
            return true;
        }

        return switch (args[3].toLowerCase()) {
            case "add" -> playersAdd(player, region, subArea, args);
            case "remove" -> playersRemove(player, region, subArea, args);
            case "flags" -> playersFlags(player, region, subArea, args);
            default -> {
                Messages.send(player, "commands.subareas.1",
                        "/hs subareas conf [subarea name] players [add|remove|flags] ...");
                yield true;
            }
        };
    }

    private boolean playersAdd(Player player, Region region, SubArea subArea, String[] args) {
        if (args.length < 5) {
            Messages.send(player, "commands.subareas.1", "/hs subareas conf [subarea name] players add [player]");
            return true;
        }

        if (!player.hasPermission("homestead.actions.regions.subareas.players.add")) {
            Messages.send(player, "common.no_permission");
            return true;
        }

        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(args[4]);

        if (target == null) {
            Messages.send(player, "commands.subareas.19", args[4]);
            return true;
        }

        if (region.isOwner(target)) {
            Messages.send(player, "commands.subareas.20");
            return true;
        }

        if (!MemberManager.isMemberOfRegion(region, target)) {
            Messages.send(player, "commands.subareas.21");
            return true;
        }

        if (MemberManager.isMemberOfSubArea(subArea, target)) {
            Messages.send(player, "commands.subareas.22");
            return true;
        }

        MemberManager.addMemberToSubArea(target, subArea);

        Messages.send(player, "commands.subareas.23", target.getName());

        LogManager.addLog(region, player, LogManager.PredefinedLog.ADD_PLAYER_SUBAREA,
                target.getName(), subArea.getName());

        Homestead.callEvent(new PlayerJoinSubAreaEvent(subArea, target));

        return true;
    }

    private boolean playersRemove(Player player, Region region, SubArea subArea, String[] args) {
        if (args.length < 5) {
            Messages.send(player, "commands.subareas.1", "/hs subareas conf [subarea name] players remove [player]");
            return true;
        }

        if (!player.hasPermission("homestead.actions.regions.subareas.players.remove")) {
            Messages.send(player, "common.no_permission");
            return true;
        }

        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(args[4]);

        if (target == null) {
            Messages.send(player, "commands.subareas.19", args[4]);
            return true;
        }

        if (!MemberManager.isMemberOfSubArea(subArea, target)) {
            Messages.send(player, "commands.subareas.24");
            return true;
        }

        MemberManager.removeMemberFromSubArea(target, subArea);

        Messages.send(player, "commands.subareas.25", target.getName());

        LogManager.addLog(region, player, LogManager.PredefinedLog.REMOVE_PLAYER_SUBAREA,
                target.getName(), subArea.getName());

        Homestead.callEvent(new PlayerLeftSubAreaEvent(subArea, player));

        return true;
    }

    private boolean playersFlags(Player player, Region region, SubArea subArea, String[] args) {
        if (args.length < 6) {
            Messages.send(player, "commands.subareas.1",
                    "/hs subareas conf [subarea name] players flags [player] [flag] (state)");
            return true;
        }

        if (!player.hasPermission("homestead.actions.regions.subareas.update.flags.members")) {
            Messages.send(player, "common.no_permission");
            return true;
        }

        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(args[4]);

        if (target == null) {
            Messages.send(player, "commands.subareas.19", args[4]);
            return true;
        }

        RegionMember member = MemberManager.getMemberOfSubArea(subArea, target);

        if (member == null) {
            Messages.send(player, "commands.subareas.24");
            return true;
        }

        String flagInput = args[5];

        if (!PlayerFlags.getFlags().contains(flagInput)) {
            Messages.send(player, "commands.subareas.16");
            return true;
        }

        long flags = member.getPlayerFlags();
        long flag = PlayerFlags.valueOf(flagInput);

        boolean denyState = resolveDenyState(FlagsCalculator.isFlagSet(flags, flag),
                args.length > 6 ? args[6] : null);

        member.setPlayerFlags(denyState ? FlagsCalculator.removeFlag(flags, flag)
                : FlagsCalculator.addFlag(flags, flag));

        Messages.send(player, "commands.subareas.18", flagInput, Formatter.getFlagState(!denyState),
                member.getPlayerName(), subArea.getName());

        LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE,
                flagInput, member.getPlayerName(), Formatter.getFlagState(!denyState));

        return true;
    }

    private boolean resolveDenyState(boolean currentDenyState, String stateInput) {
        if (stateInput == null) {
            return currentDenyState;
        }

        return switch (stateInput.toLowerCase()) {
            case "1", "t", "true", "allow" -> false;
            case "0", "f", "false", "deny" -> true;
            default -> currentDenyState;
        };
    }

    private List<String> getMemberNames(Player player) {
        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            return new ArrayList<>();
        }

        List<String> names = new ArrayList<>();

        for (RegionMember member : MemberManager.getMembersOfRegion(region)) {
            OfflinePlayer bukkitMember = member.getPlayer();

            if (bukkitMember != null) {
                names.add(bukkitMember.getName());
            }
        }

        return names;
    }
}




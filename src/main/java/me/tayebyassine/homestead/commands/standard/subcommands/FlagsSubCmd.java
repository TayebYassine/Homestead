package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.flags.FlagCalculator;
import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.gui.menus.GlobalPlayerFlags;
import me.tayebyassine.homestead.gui.menus.RegionWorldFlags;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionMember;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.FlagsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs flags}) that manages player, global, and world flags
 * of the current region.
 */
public final class FlagsSubCmd extends SubCommandBuilder {

    private static final List<String> SET_TYPES = List.of("member", "global", "world");
    private static final List<String> FLAG_STATES = List.of("allow", "deny");
    private static final List<String> ALLOW_INPUTS = List.of("1", "t", "true", "allow");
    private static final List<String> DENY_INPUTS = List.of("0", "f", "false", "deny");

    public FlagsSubCmd() {
        super("flags");
        setRegionPermission();
        setUsage("/hs flags [global/world/member] {member} [flag] (allow/deny)");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.flags.0");
            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.flags.1");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "member" -> handleMemberFlags(player, region, args);
            case "global" -> handleGlobalFlags(player, region, args);
            case "world" -> handleWorldFlags(player, region, args);
            default -> Messages.send(player, "commands.flags.4", getUsage());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return new ArrayList<>();
        }

        List<String> suggestions = new ArrayList<>();
        String setType = args[0].toLowerCase();

        switch (args.length) {
            case 1 -> suggestions.addAll(SET_TYPES);
            case 2 -> {
                switch (setType) {
                    case "member" -> suggestions.addAll(getMemberNames(player));
                    case "global" -> suggestions.addAll(PlayerFlag.getFlags());
                    case "world" -> suggestions.addAll(WorldFlag.getFlags());
                }
            }
            case 3 -> {
                if (setType.equals("member")) {
                    suggestions.addAll(PlayerFlag.getFlags());
                } else if (setType.equals("global") || setType.equals("world")) {
                    suggestions.addAll(FLAG_STATES);
                }
            }
            case 4 -> {
                if (setType.equals("member")) {
                    suggestions.addAll(FLAG_STATES);
                }
            }
        }

        return suggestions;
    }

    private void handleMemberFlags(Player player, Region region, String[] args) {
        if (!checkPermission(player, region, "homestead.actions.regions.update.flags.members",
                ControlFlag.SET_MEMBER_FLAGS.getBitmask())) {
            return;
        }

        if (args.length < 3) {
            Messages.send(player, "commands.flags.4", getUsage());
            return;
        }

        OfflinePlayer target = resolveTarget(player, args[1]);

        if (target == null) {
            return;
        }

        RegionMember member = MemberManager.getMemberOfRegion(region, target);

        if (member == null) {
            Messages.send(player, "commands.flags.6");
            return;
        }

        if (!canModifyMemberFlags(player, region, target)) {
            Messages.send(player, "commands.flags.7");
            return;
        }

        String flagInput = args[2];

        if (!isValidPlayerFlag(player, flagInput)) {
            return;
        }

        long flag = PlayerFlag.parse(flagInput);
        boolean newState = toggleFlag(member.getPlayerFlags(), flag, args, 3);

        member.setPlayerFlags(applyFlag(member.getPlayerFlags(), flag, newState));

        Messages.send(player, "commands.flags.10", flagInput, Formatter.getFlagState(newState),
                target.getName(), region.getName());

        LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE,
                flagInput, member.getPlayerName(), Formatter.getFlagState(newState));
    }

    private void handleGlobalFlags(Player player, Region region, String[] args) {
        if (!checkPermission(player, region, "homestead.actions.regions.update.flags.global",
                ControlFlag.SET_GLOBAL_FLAGS.getBitmask())) {
            return;
        }

        if (args.length == 1) {
            new GlobalPlayerFlags(player, region);
            return;
        }

        String flagInput = args[1];

        if (!isValidPlayerFlag(player, flagInput)) {
            return;
        }

        long flag = PlayerFlag.parse(flagInput);
        boolean newState = toggleFlag(region.getPlayerFlags(), flag, args, 2);

        region.setPlayerFlags(applyFlag(region.getPlayerFlags(), flag, newState));

        Messages.send(player, "commands.flags.11", flagInput, Formatter.getFlagState(newState), region.getName());

        LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE,
                flagInput, region.getName(), Formatter.getFlagState(newState));
    }

    private void handleWorldFlags(Player player, Region region, String[] args) {
        if (!checkPermission(player, region, "homestead.actions.regions.update.flags.world",
                ControlFlag.SET_WORLD_FLAGS.getBitmask())) {
            return;
        }

        if (args.length == 1) {
            new RegionWorldFlags(player, region);
            return;
        }

        String flagInput = args[1];

        if (!isValidWorldFlag(player, flagInput)) {
            return;
        }

        long flag = WorldFlag.parse(flagInput);

        if (flag == WorldFlag.WARS.getBitmask() && Cooldown.hasCooldown(player, Cooldown.Type.WAR_FLAG_DISABLED)) {
            Cooldown.sendCooldownMessage(player);
            return;
        }

        boolean newState = toggleFlag(region.getWorldFlags(), flag, args, 2);

        region.setWorldFlags(applyFlag(region.getWorldFlags(), flag, newState));

        Messages.send(player, "commands.flags.12", flagInput, Formatter.getFlagState(newState), region.getName());

        LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE,
                flagInput, region.getName(), Formatter.getFlagState(newState));
    }

    private boolean checkPermission(Player player, Region region, String permission, long controlFlag) {
        if (!player.hasPermission(permission)) {
            Messages.send(player, "commands.flags.2");
            return false;
        }

        return PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player, controlFlag);
    }

    private OfflinePlayer resolveTarget(Player player, String targetName) {
        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

        if (target == null) {
            Messages.send(player, "commands.flags.5");
        }

        return target;
    }

    private boolean canModifyMemberFlags(Player player, Region region, OfflinePlayer target) {
        return PlayerUtility.isOperator(player) || region.isOwner(player) || !PlayerUtility.equals(player, target);
    }

    private boolean isValidPlayerFlag(Player player, String flagInput) {
        if (!PlayerFlag.getFlags().contains(flagInput)) {
            Messages.send(player, "commands.flags.8");
            return false;
        }

        return isFlagEnabled(player, flagInput);
    }

    private boolean isValidWorldFlag(Player player, String flagInput) {
        if (!WorldFlag.getFlags().contains(flagInput)) {
            Messages.send(player, "commands.flags.8");
            return false;
        }

        return isFlagEnabled(player, flagInput);
    }

    private boolean isFlagEnabled(Player player, String flagInput) {
        if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagInput)) {
            Messages.send(player, "commands.flags.9");
            return false;
        }

        return true;
    }

    private boolean toggleFlag(long currentFlags, long flag, String[] args, int stateIndex) {
        boolean currentState = FlagCalculator.isFlagSet(currentFlags, flag);

        if (args.length > stateIndex) {
            String input = args[stateIndex].toLowerCase();

            if (ALLOW_INPUTS.contains(input)) {
                return true;
            }

            if (DENY_INPUTS.contains(input)) {
                return false;
            }
        }

        return !currentState;
    }

    private long applyFlag(long flags, long flag, boolean set) {
        return set ? FlagCalculator.addFlag(flags, flag) : FlagCalculator.removeFlag(flags, flag);
    }

    private List<String> getMemberNames(Player player) {
        List<String> names = new ArrayList<>();

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            return names;
        }

        for (RegionMember member : MemberManager.getMembersOfRegion(region)) {
            OfflinePlayer bukkitMember = member.getPlayer();

            if (bukkitMember != null) {
                names.add(bukkitMember.getName());
            }
        }

        return names;
    }
}







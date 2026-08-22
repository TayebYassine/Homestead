package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.FlagCalculator;
import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionMember;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin sub-command ({@code /hsadmin flagsoverride}) that applies a flag state across
 * every region, ignoring the regular per-region permission checks.
 */
public final class FlagsOverrideSubCmd extends SubCommandBuilder {

    public FlagsOverrideSubCmd() {
        super("flagsoverride");
        setAdminPermission();
        setUsage("/hsadmin flagsoverride [global/world/member] {member} [flag] (allow/deny)");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(sender, "commands.op_flagsoverride.0", getUsage());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "member" -> handleMemberFlags(sender, args);
            case "global" -> handleGlobalFlags(sender, args);
            case "world" -> handleWorldFlags(sender, args);
            default -> Messages.send(sender, "commands.op_flagsoverride.0", getUsage());
        }

        Messages.send(sender, "commands.op_flagsoverride.3");

        return true;
    }

    private void handleMemberFlags(CommandSender sender, String[] args) {
        if (args.length < 3) {
            Messages.send(sender, "commands.op_flagsoverride.0", getUsage());
            return;
        }

        String targetName = args[1];
        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

        if (target == null) {
            Messages.send(sender, "commands.op_flagsoverride.1");
            return;
        }

        long flag = parsePlayerFlag(sender, args[2]);
        if (flag == -1) {
            return;
        }

        String stateInput = args.length > 3 ? args[3] : null;

        for (Region region : RegionManager.getAll()) {
            RegionMember member = MemberManager.getMemberOfRegion(region, target);

            if (member == null) {
                continue;
            }

            long flags = member.getPlayerFlags();
            boolean targetState = resolveTargetState(FlagCalculator.isFlagSet(flags, flag), stateInput);

            member.setPlayerFlags(targetState
                    ? FlagCalculator.addFlag(flags, flag)
                    : FlagCalculator.removeFlag(flags, flag));

            Messages.send(sender, "commands.op_flagsoverride.4",
                    args[2], Formatter.getFlagState(targetState), targetName, region.getName());
        }
    }

    private void handleGlobalFlags(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Messages.send(sender, "commands.op_flagsoverride.0", getUsage());
            return;
        }

        long flag = parsePlayerFlag(sender, args[1]);
        if (flag == -1) {
            return;
        }

        String stateInput = args.length > 2 ? args[2] : null;

        for (Region region : RegionManager.getAll()) {
            long flags = region.getPlayerFlags();
            boolean targetState = resolveTargetState(FlagCalculator.isFlagSet(flags, flag), stateInput);

            region.setPlayerFlags(targetState
                    ? FlagCalculator.addFlag(flags, flag)
                    : FlagCalculator.removeFlag(flags, flag));

            Messages.send(sender, "commands.op_flagsoverride.5",
                    args[1], Formatter.getFlagState(targetState), region.getName());
        }
    }

    private void handleWorldFlags(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Messages.send(sender, "commands.op_flagsoverride.0", getUsage());
            return;
        }

        String flagInput = args[1];

        if (!WorldFlag.getFlags().contains(flagInput)) {
            Messages.send(sender, "commands.op_flagsoverride.2");
            return;
        }

        long flag = WorldFlag.parse(flagInput);
        String stateInput = args.length > 2 ? args[2] : null;

        for (Region region : RegionManager.getAll()) {
            long flags = region.getWorldFlags();
            boolean targetState = resolveTargetState(FlagCalculator.isFlagSet(flags, flag), stateInput);

            region.setWorldFlags(targetState
                    ? FlagCalculator.addFlag(flags, flag)
                    : FlagCalculator.removeFlag(flags, flag));

            Messages.send(sender, "commands.op_flagsoverride.6",
                    flagInput, Formatter.getFlagState(targetState), region.getName());
        }
    }

    private long parsePlayerFlag(CommandSender sender, String flagInput) {
        if (!PlayerFlag.getFlags().contains(flagInput)) {
            Messages.send(sender, "commands.op_flagsoverride.2");
            return -1;
        }
        return PlayerFlag.parse(flagInput);
    }

    private boolean resolveTargetState(boolean currentState, String stateInput) {
        return stateInput == null ? !currentState : parseFlagState(stateInput);
    }

    private boolean parseFlagState(String input) {
        return switch (input.toLowerCase()) {
            case "1", "t", "true", "allow" -> true;
            default -> false;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(List.of("member", "global", "world"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "member" -> {
                    if (player != null) {
                        suggestions.addAll(getAllMemberNames());
                    }
                }
                case "global" -> suggestions.addAll(PlayerFlag.getFlags());
                case "world" -> suggestions.addAll(WorldFlag.getFlags());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("member")) {
            suggestions.addAll(PlayerFlag.getFlags());
        } else if ((args.length == 3 && (args[0].equalsIgnoreCase("global") || args[0].equalsIgnoreCase("world")))
                || (args.length == 4 && args[0].equalsIgnoreCase("member"))) {
            suggestions.addAll(List.of("allow", "deny"));
        }

        return suggestions;
    }

    private List<String> getAllMemberNames() {
        List<String> names = new ArrayList<>();

        for (Region region : RegionManager.getAll()) {
            for (RegionMember member : MemberManager.getMembersOfRegion(region)) {
                OfflinePlayer bukkitMember = member.getPlayer();
                if (bukkitMember != null) {
                    names.add(bukkitMember.getName());
                }
            }
        }

        return names;
    }
}









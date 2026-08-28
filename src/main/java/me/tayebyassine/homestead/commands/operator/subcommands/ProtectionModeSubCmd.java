package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.ProtectionMode;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;

/**
 * Admin sub-command ({@code /hsadmin protectionmode}) that toggles the plugin-wide lock-down state.
 */
public final class ProtectionModeSubCmd extends SubCommandBuilder {

    public ProtectionModeSubCmd() {
        super("protectionmode");
        setAdminPermission();
        setUsage("/hsadmin protectionmode [true|false]");
        setAllowedCommandSenders(CommandSenderType.PLAYER, CommandSenderType.CONSOLE);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Messages.send(sender, "commands.op_protectionmode.4", ProtectionMode.isEnabled() ? "enabled" : "disabled");
            return true;
        }

        Boolean target = parse(args[0]);

        if (target == null) {
            Messages.send(sender, "commands.op_protectionmode.0", getUsage());
            return true;
        }

        if (target == ProtectionMode.isEnabled()) {
            Messages.send(sender, "commands.op_protectionmode.3", target ? "enabled" : "disabled");
            return true;
        }

        ProtectionMode.setEnabled(target);

        if (target) {
            Messages.send(sender, "commands.op_protectionmode.1");
        } else {
            Messages.send(sender, "commands.op_protectionmode.2");
        }

        return true;
    }

    private Boolean parse(String value) {
        return switch (value.toLowerCase()) {
            case "true", "on", "enable", "enabled", "yes", "1" -> true;
            case "false", "off", "disable", "disabled", "no", "0" -> false;
            default -> null;
        };
    }
}

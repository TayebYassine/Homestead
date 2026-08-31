package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.BankWithdrawEvent;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.managers.WarManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.NumberUtils;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerBank;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs withdraw}) that withdraws money from the current region's bank
 * into the player's balance.
 */
public final class WithdrawBankSubCmd extends SubCommandBuilder {

    public WithdrawBankSubCmd() {
        super("withdraw");
        setRegionPermission("homestead.actions.regions.withdraw_bank");
        setUsage("/hs withdraw [amount/all]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.withdraw.0");
            return true;
        }

        if (!Homestead.VAULT.isEconomyReady()) {
            Messages.send(player, "commands.withdraw.1");

            Logger.warning(Logger.PredefinedMessage.ECONOMY_INTEGRATION_DISABLED);

            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.withdraw.2");
            return true;
        }

        if (WarManager.isRegionInWar(region)) {
            Messages.send(player, "commands.withdraw.3");
            return true;
        }

        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.WITHDRAW_MONEY, true)) {
            return true;
        }

        String amountInput = args[0];

        if (!amountInput.equalsIgnoreCase("all") && !NumberUtils.isValidDouble(amountInput)) {
            Messages.send(player, "commands.withdraw.5");
            return true;
        }

        double amount = amountInput.equalsIgnoreCase("all")
                ? region.getBank()
                : Double.parseDouble(amountInput);

        if (!Double.isFinite(amount) || amount <= 0) {
            Messages.send(player, "commands.withdraw.6");
            return true;
        }

        if (amount > region.getBank()) {
            Messages.send(player, "commands.withdraw.7");
            return true;
        }

        PlayerBank.deposit(player, amount);
        region.withdrawBank(amount);

        Messages.send(player, "commands.withdraw.8", Formatter.getBalance(amount), region.getName());

        Homestead.callEvent(new BankWithdrawEvent(region, amount));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return new ArrayList<>();
        }

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("all");
        }

        return suggestions;
    }
}





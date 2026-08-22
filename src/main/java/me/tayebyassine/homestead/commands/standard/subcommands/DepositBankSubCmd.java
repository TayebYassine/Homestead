package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.BankDepositEvent;
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
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import me.tayebyassine.homestead.util.minecraft.players.PlayerBank;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs deposit}) that deposits money from the player's balance
 * into the current region's bank.
 */
public final class DepositBankSubCmd extends SubCommandBuilder {

    public DepositBankSubCmd() {
        super("deposit");
        setRegionPermission("homestead.actions.regions.deposit_bank");
        setUsage("/hs deposit [amount/all]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.deposit.0");
            return true;
        }

        if (!Homestead.VAULT.isEconomyReady()) {
            Messages.send(player, "commands.deposit.1");

            Logger.warning(Logger.PredefinedMessage.ECONOMY_INTEGRATION_DISABLED);

            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.deposit.2");
            return true;
        }

        if (WarManager.isRegionInWar(region)) {
            Messages.send(player, "commands.deposit.3");
            return true;
        }

        if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                ControlFlag.DEPOSIT_MONEY.getBitmask())) {
            return true;
        }

        String amountInput = args[0];

        if (!amountInput.equalsIgnoreCase("all") && !NumberUtils.isValidDouble(amountInput)) {
            Messages.send(player, "commands.deposit.5");
            return true;
        }

        double amount = amountInput.equalsIgnoreCase("all")
                ? PlayerBank.get(player)
                : Double.parseDouble(amountInput);

        if (!Double.isFinite(amount) || amount <= 0) {
            Messages.send(player, "commands.deposit.6");
            return true;
        }

        if (amount > PlayerBank.get(player)) {
            Messages.send(player, "commands.deposit.7");
            return true;
        }

        if ((amount + region.getBank()) >= Limits.getRegionLimit(region, Limits.LimitType.MAX_BANK_DEPOSIT)) {
            Messages.send(player, "commands.deposit.8");
            return true;
        }

        PlayerBank.withdraw(player, amount);
        region.depositBank(amount);

        Messages.send(player, "commands.deposit.9", Formatter.getBalance(amount), region.getName());

        Homestead.callEvent(new BankDepositEvent(region, amount));

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





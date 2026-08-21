package me.tayebyassine.homestead.commands.standard;

import me.tayebyassine.homestead.commands.CommandSenderType;

import me.tayebyassine.homestead.commands.CommandBuilder;
import me.tayebyassine.homestead.commands.standard.subcommands.*;
import me.tayebyassine.homestead.gui.menus.RegionsMenu;
import me.tayebyassine.homestead.util.java.StringSimilarity;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Main player command ({@code /region}, {@code /rg}, {@code /hs}, {@code /homestead})
 * exposing every region management sub-command.
 */
public final class RegionCommand extends CommandBuilder {

    public RegionCommand() {
        super("region", "rg", "hs", "homestead");
        setPermission("homestead.commands.region");
        setUsage("/region [sub-command]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);

        registerSubCommand(new CreateRegionSubCmd());
        registerSubCommand(new DeleteRegionSubCmd());
        registerSubCommand(new SetRegionSubCmd());
        registerSubCommand(new RenameRegionSubCmd());
        registerSubCommand(new BordersSubCmd());
        registerSubCommand(new BanPlayerSubCmd());
        registerSubCommand(new UnbanPlayerSubCmd());
        registerSubCommand(new TrustPlayerSubCmd());
        registerSubCommand(new UntrustPlayerSubCmd());
        registerSubCommand(new FlagsSubCmd());
        registerSubCommand(new AcceptInviteSubCmd());
        registerSubCommand(new DenyInviteSubCmd());
        registerSubCommand(new VisitRegionSubCmd());
        registerSubCommand(new SubAreasSubCmd());
        registerSubCommand(new DepositBankSubCmd());
        registerSubCommand(new WithdrawBankSubCmd());
        registerSubCommand(new MenuSubCmd());
        registerSubCommand(new PlayerInfoSubCmd());
        registerSubCommand(new HomeSubCmd());
        registerSubCommand(new RegionInfoSubCmd());
        registerSubCommand(new BanlistSubCmd());
        registerSubCommand(new MembersSubCmd());
        registerSubCommand(new ClaimlistSubCmd());
        registerSubCommand(new HelpSubCmd());
        registerSubCommand(new LogsSubCmd());
        registerSubCommand(new RateRegionSubCmd());
        registerSubCommand(new TopRegionsSubCmd());
        registerSubCommand(new AutoSubCmd());
        registerSubCommand(new KickPlayerSubCmd());
        registerSubCommand(new WarSubCmd());
        registerSubCommand(new ChatSubCmd());
        registerSubCommand(new MailSubCmd());
        registerSubCommand(new BalanceSubCmd());
        registerSubCommand(new RewardsSubCmd());
        registerSubCommand(new MergeRegionSubCmd());
        registerSubCommand(new MergeAcceptRegionSubCmd());
        registerSubCommand(new LevelsSubCmd());
        registerSubCommand(new LeaveRegionSubCmd());
        registerSubCommand(new FlySubCmd());
        registerSubCommand(new StorageSubCmd());
        registerSubCommand(new SetTimeSubCmd());
        registerSubCommand(new SetWeatherSubCmd());
        registerSubCommand(new SetDescriptionSubCmd());
        registerSubCommand(new SetDisplayNameSubCmd());
        registerSubCommand(new SetMapColorSubCmd());
        registerSubCommand(new SetMapIconSubCmd());
        registerSubCommand(new SetMemberTaxSubCmd());
        registerSubCommand(new SetSpawnSubCmd());
    }

    @Override
    public boolean onDefaultExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length == 0) {
            new RegionsMenu(player);
            return true;
        }

        String attempted = args[0].toLowerCase();
        String similarity = String.join(", ",
                StringSimilarity.find(getSubCommandNames(), attempted)
        );

        Messages.send(player, "commands.homestead.0", similarity);

        return true;
    }
}




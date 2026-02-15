package tfagaming.projects.minecraft.homestead.commands.standard;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.commands.standard.subcommands.*;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionsMenu;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringSimilarity;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.List;

public class RegionCommand extends CommandBuilder {
	public RegionCommand() {
		super("region", "rg", "hs", "homestead");

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
	}

	@Override
	public boolean onDefaultExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (args.length == 0) {
			new RegionsMenu(player);
			return true;
		}

		String attempted = args[0].toLowerCase();
		String similarity = String.join(", ",
				StringSimilarity.find(getSubCommandNames(), attempted)
		);

		Messages.send(player, 7, new Placeholder()
				.add("{similarity-subcmds}", similarity)
		);

		return true;
	}

	@Override
	public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
		return super.onDefaultTabComplete(sender, args);
	}
}
package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.TopBankRegionsMenu;

public class TopRegionsSubCmd extends SubCommandBuilder {
	public TopRegionsSubCmd() {
		super("top");
		setUsage("/region top");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		new TopBankRegionsMenu(player);

		return true;
	}
}

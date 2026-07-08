package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.AutoClaimSession;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.List;

public class AutoSubCmd extends SubCommandBuilder {
	public AutoSubCmd() {
		super("auto");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.create",
				"homestead.actions.regions.chunks.claim"
		));
		setUsage("/hs auto");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.auto.2");
			return true;
		}

		if (AutoClaimSession.hasSession(player)) {
			AutoClaimSession.removeSession(player);

			Messages.send(player, "commands.auto.1");
		} else {
			AutoClaimSession.newSession(player);

			Messages.send(player, "commands.auto.0", region.getName());
		}

		return true;
	}
}

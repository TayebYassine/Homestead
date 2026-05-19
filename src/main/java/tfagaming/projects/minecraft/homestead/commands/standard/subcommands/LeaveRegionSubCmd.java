package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.PlayerLeftRegionEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.List;

public class LeaveRegionSubCmd extends SubCommandBuilder {
	public LeaveRegionSubCmd() {
		super("leave");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/hs leave [confirm]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			Messages.send(player, "commands.leave.0");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.leave.1");
			return true;
		}

		if (region.isOwner(player)) {
			Messages.send(player, "commands.leave.3");
			return true;
		}

		if (!MemberManager.isMemberOfRegion(region, player)) {
			Messages.send(player, "commands.leave.2");
			return true;
		}

		String confirmInput = args[0];

		if (!confirmInput.equalsIgnoreCase("confirm")) {
			Messages.send(player, "commands.leave.0");
			return true;
		}

		MemberManager.removeMemberFromRegion(player, region);

		LogManager.addLog(region, player, LogManager.PredefinedLog.UNTRUST_PLAYER, player.getName());

		Messages.send(player, "commands.leave.4", region.getName());

		TargetRegionSession.randomizeRegion(player);

		Homestead.callEvent(new PlayerLeftRegionEvent(region, player));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.add("confirm");
		}

		return suggestions;
	}
}

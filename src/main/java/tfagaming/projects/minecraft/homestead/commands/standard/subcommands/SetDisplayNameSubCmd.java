package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionDisplaynameUpdateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionLocationUpdateEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.Arrays;
import java.util.List;

public class SetDisplayNameSubCmd extends SubCommandBuilder {
	public SetDisplayNameSubCmd() {
		super("setdisplayname");
		setUsage("/region setdisplayname [name]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		List<String> regionDisplayNameList = Arrays.asList(args).subList(0, args.length);
		String regionDisplayName = String.join(" ", regionDisplayNameList);

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.RENAME_REGION)) {
			return true;
		}

		if (!StringUtils.isValidRegionDisplayName(regionDisplayName)) {
			Messages.send(player, 14);
			return true;
		}

		if (region.getDisplayName() != null && region.getDisplayName().equals(regionDisplayName)) {
			Messages.send(player, 11);
			return true;
		}

		if (ColorTranslator.containsMiniMessageTag(regionDisplayName)) {
			Messages.send(player, 30);
			return true;
		}

		final String oldDisplayName = region.getDisplayName() == null ? Formatter.getNone() : region.getDisplayName();

		Cooldown.startCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE);

		region.setDisplayName(regionDisplayName);

		Messages.send(player, 15, new Placeholder()
				.add("{olddisplayname}", oldDisplayName)
				.add("{newdisplayname}", regionDisplayName)
		);

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_DISPLAYNAME, regionDisplayName);

		Homestead.callEvent(new RegionDisplaynameUpdateEvent(region, oldDisplayName, regionDisplayName));

		return true;
	}
}

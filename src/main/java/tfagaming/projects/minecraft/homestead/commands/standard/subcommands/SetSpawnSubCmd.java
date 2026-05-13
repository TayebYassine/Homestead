package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionLocationUpdateEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.List;

public class SetSpawnSubCmd extends SubCommandBuilder {
	public SetSpawnSubCmd() {
		super("setspawn");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.update.spawn"
		));
		setUsage("/region setspawn");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			reply(player, "setspawn.0");
			return true;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_SPAWN_CHANGE)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.SET_SPAWN)) {
			reply(player, "setspawn.1");
			return true;
		}

		Location location = player.getLocation();

		Chunk chunk = location.getChunk();

		if (!ChunkManager.isChunkClaimedByRegion(region, chunk)) {
			reply(player, "setspawn.2", region.getName());
			return true;
		}

		Cooldown.startCooldown(player, Cooldown.Type.REGION_SPAWN_CHANGE);

		final Location oldLocation = region.getLocation() == null ? null : region.getLocation().toBukkit();

		region.setLocation(location);

		reply(player, "setspawn.3");

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_SPAWN);

		Homestead.callEvent(new RegionLocationUpdateEvent(region, oldLocation, location));

		return true;
	}
}

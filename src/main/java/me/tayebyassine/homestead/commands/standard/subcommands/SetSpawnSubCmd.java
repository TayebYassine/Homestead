package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RegionLocationUpdateEvent;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.ControlFlags;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.tools.java.Formatter;
import me.tayebyassine.homestead.tools.java.Placeholder;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerUtility;

import java.util.List;

public class SetSpawnSubCmd extends SubCommandBuilder {
	public SetSpawnSubCmd() {
		super("setspawn");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.update.spawn"
		));
		setUsage("/hs setspawn");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.setspawn.0");
			return true;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_SPAWN_CHANGE)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.SET_SPAWN)) {
			return true;
		}

		Location location = player.getLocation();

		Chunk chunk = location.getChunk();

		if (!ChunkManager.isChunkClaimedByRegion(region, chunk)) {
			Messages.send(player, "commands.setspawn.2", region.getName());
			return true;
		}

		Cooldown.startCooldown(player, Cooldown.Type.REGION_SPAWN_CHANGE);

		final Location oldLocation = region.getLocation() == null ? null : region.getLocation().toBukkit();

		region.setLocation(location);

		Messages.send(player, "commands.setspawn.3");

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_SPAWN);

		Homestead.callEvent(new RegionLocationUpdateEvent(region, oldLocation, location));

		return true;
	}
}

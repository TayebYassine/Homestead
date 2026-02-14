package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import biz.princeps.landlord.api.ILandLord;
import biz.princeps.landlord.api.IOwnedLand;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.ChunkCoordinate;
import me.angeschossen.lands.api.land.Land;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.ClaimWorld;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.*;

public class ImportSubCmd extends SubCommandBuilder {
	public ImportSubCmd() {
		super("import");
	}

	public static boolean isGriefPreventionInstalled() {
		try {
			GriefPrevention.instance.getClass();

			return Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention") != null
					&& Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention").isEnabled();
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}

	public static boolean isLandLordInstalled() {
		try {
			return Bukkit.getServer().getPluginManager().getPlugin("LandLord") != null
					&& Bukkit.getServer().getPluginManager().getPlugin("LandLord").isEnabled();
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}

	public static boolean isClaimChunkInstalled() {
		try {
			ClaimChunk.getInstance();

			return Bukkit.getServer().getPluginManager().getPlugin("ClaimChunk") != null
					&& Bukkit.getServer().getPluginManager().getPlugin("ClaimChunk").isEnabled();
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}

	public static boolean isLandsInstalled() {
		try {
			return Bukkit.getServer().getPluginManager().getPlugin("Lands") != null
					&& Bukkit.getServer().getPluginManager().getPlugin("Lands").isEnabled();
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}

	public static boolean isHuskClaimsInstalled() {
		try {
			return Bukkit.getServer().getPluginManager().getPlugin("HuskClaims") != null
					&& Bukkit.getServer().getPluginManager().getPlugin("HuskClaims").isEnabled();
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (args.length < 2) {
			Messages.send(sender, 0);
			return true;
		}

		String pluginInput = args[1];

		switch (pluginInput.toLowerCase()) {
			case "griefprevention": {
				if (!isGriefPreventionInstalled()) {
					Messages.send(sender, 114);
					return true;
				}

				int imported = 0;

				Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims();

				for (Claim claim : claims) {
					OfflinePlayer owner = Homestead.getInstance().getOfflinePlayerSync(claim.getOwnerID());

					if (owner == null) {
						continue;
					}

					Region region = RegionsManager.createRegion(owner.getName(),
							Bukkit.getOfflinePlayer(claim.getOwnerID()),
							true);

					for (Chunk chunk : claim.getChunks()) {
						if (!ChunksManager.isChunkClaimed(chunk)) {
							ChunksManager.claimChunk(region.getUniqueId(), chunk);
						}
					}

					for (OfflinePlayer player : Homestead.getInstance().getOfflinePlayersSync()) {
						if (player.getUniqueId().equals(owner.getUniqueId())) {
							continue;
						}

						ClaimPermission permission = claim.getPermission(player.getUniqueId().toString());

						if (!region.isPlayerMember(player) && permission != null) {
							region.addMember(player);

							region.setMemberFlags(region.getMember(player), region.getPlayerFlags());
						}
					}

					Logger.info("Imported a region; Name = " + region.getName() + ", ID = "
							+ region.getUniqueId().toString() + ", Owner = " + owner.getName() + " ("
							+ owner.getUniqueId() + ")");

					imported++;
				}

				Map<String, String> replacements = new HashMap<>();
				replacements.put("{regions}", String.valueOf(imported));

				Messages.send(sender, 115, replacements);

				break;
			}
			case "landlord": {
				if (!isLandLordInstalled()) {
					Messages.send(sender, 114);
					return true;
				}

				int imported = 0;

				ILandLord landlord = getLandLordInstance();

				Set<IOwnedLand> chunks = landlord.getWGManager().getRegions();

				for (IOwnedLand chunk : chunks) {
					OfflinePlayer owner = Bukkit.getOfflinePlayer(chunk.getOwner());

					if (RegionsManager.getRegionsOwnedByPlayer(owner).isEmpty()) {
						Region region = RegionsManager.createRegion(owner.getName(),
								owner, true);

						if (!ChunksManager.isChunkClaimed(chunk.getChunk())) {
							ChunksManager.claimChunk(region.getUniqueId(), chunk.getChunk());
						}

						for (UUID friendUuid : chunk.getFriends()) {
							OfflinePlayer friend = Homestead.getInstance().getOfflinePlayerSync(friendUuid);

							if (friend == null) {
								continue;
							}

							if (friend.getUniqueId().equals(owner.getUniqueId())) {
								continue;
							}

							if (!region.isPlayerMember(friend)) {
								region.addMember(friend);

								region.setMemberFlags(region.getMember(friend), region.getPlayerFlags());
							}
						}

						Logger.info("Imported a region; Name = " + region.getName() + ", ID = "
								+ region.getUniqueId().toString() + ", Owner = " + owner.getName() + " ("
								+ owner.getUniqueId() + ")");

						imported++;
					} else {
						Region region = RegionsManager.getRegionsOwnedByPlayer(owner).getFirst();

						for (UUID friendUuid : chunk.getFriends()) {
							OfflinePlayer friend = Homestead.getInstance().getOfflinePlayerSync(friendUuid);

							if (friend == null) {
								continue;
							}

							if (friend.getUniqueId().equals(owner.getUniqueId())) {
								continue;
							}

							if (!region.isPlayerMember(friend)) {
								region.addMember(friend);

								region.setMemberFlags(region.getMember(friend), region.getPlayerFlags());
							}
						}

						if (!ChunksManager.isChunkClaimed(chunk.getChunk())) {
							ChunksManager.claimChunk(region.getUniqueId(), chunk.getChunk());
						}
					}
				}

				Map<String, String> replacements = new HashMap<>();
				replacements.put("{regions}", String.valueOf(imported));

				Messages.send(sender, 115, replacements);

				break;
			}
			case "claimchunk": {
				if (!isClaimChunkInstalled()) {
					Messages.send(sender, 114);
					return true;
				}

				int imported = 0;

				ClaimChunk claimChunk = ClaimChunk.getInstance();

				for (OfflinePlayer offlinePlayer : Homestead.getInstance().getOfflinePlayersSync()) {
					if (offlinePlayer.getName() == null) {
						continue;
					}

					ChunkPos[] chunkPositions = claimChunk.getChunkHandler()
							.getClaimedChunks(offlinePlayer.getUniqueId());

					Region region = RegionsManager.createRegion(offlinePlayer.getName(), offlinePlayer, true);

					for (ChunkPos chunkPos : chunkPositions) {
						Chunk chunk = ChunksManager.getFromLocation(Bukkit.getWorld(chunkPos.world()), chunkPos.x(),
								chunkPos.z());

						if (!ChunksManager.isChunkClaimed(chunk)) {
							ChunksManager.claimChunk(region.getUniqueId(), chunk);
						}
					}

					Logger.info("Imported a region; Name = " + region.getName() + ", ID = "
							+ region.getUniqueId().toString() + ", Owner = " + offlinePlayer.getName() + " ("
							+ offlinePlayer.getUniqueId() + ")");

					imported++;
				}

				Map<String, String> replacements = new HashMap<>();
				replacements.put("{regions}", String.valueOf(imported));

				Messages.send(sender, 115, replacements);

				break;
			}
			case "lands": {
				if (!isLandsInstalled()) {
					Messages.send(sender, 114);
					return true;
				}

				int imported = 0;

				LandsIntegration landsApi = getLandsInstance();

				for (Land land : landsApi.getLands()) {
					OfflinePlayer owner = Homestead.getInstance().getOfflinePlayerSync(land.getOwnerUID());

					if (owner == null) {
						continue;
					}

					Region region = RegionsManager.createRegion(owner.getName(), owner, true);

					for (World world : Bukkit.getWorlds()) {
						for (ChunkCoordinate chunkCoord : Objects.requireNonNull(land.getChunks(world))) {
							Chunk chunk = ChunksManager.getFromLocation(world, chunkCoord.getX(), chunkCoord.getZ());

							if (!ChunksManager.isChunkClaimed(chunk)) {
								ChunksManager.claimChunk(region.getUniqueId(), chunk);
							}
						}
					}

					for (UUID trustedUuid : land.getTrustedPlayers()) {
						OfflinePlayer trusted = Homestead.getInstance().getOfflinePlayerSync(trustedUuid);

						if (trusted == null) {
							continue;
						}

						if (trusted.getUniqueId().equals(owner.getUniqueId())) {
							continue;
						}

						if (!region.isPlayerMember(trusted)) {
							region.addMember(trusted);

							region.setMemberFlags(region.getMember(trusted), region.getPlayerFlags());
						}
					}

					Logger.info("Imported a region; Name = " + region.getName() + ", ID = "
							+ region.getUniqueId().toString() + ", Owner = " + owner.getName() + " ("
							+ owner.getUniqueId() + ")");

					imported++;
				}

				Map<String, String> replacements = new HashMap<>();
				replacements.put("{regions}", String.valueOf(imported));

				Messages.send(sender, 115, replacements);

				break;
			}
			// this api gave me headache, please do not touch it
			case "huskclaims": {
				if (!isHuskClaimsInstalled()) {
					Messages.send(sender, 114);
					return true;
				}

				int imported = 0;

				BukkitHuskClaimsAPI api = BukkitHuskClaimsAPI.getInstance();

				for (OfflinePlayer offlinePlayer : Homestead.getInstance().getOfflinePlayersSync()) {
					if (offlinePlayer.getName() == null) {
						continue;
					}

					Region region = RegionsManager.createRegion(offlinePlayer.getName(), offlinePlayer, true);

					for (World world : Bukkit.getWorlds()) {
						net.william278.huskclaims.position.World hcWorld = api.getWorld(world.getName());

						Optional<ClaimWorld> claimWorld = api.getClaimWorld(hcWorld);

						claimWorld.ifPresent(claimWorld1 -> {
							claimWorld1.getClaims().forEach(claim -> {
								net.william278.huskclaims.claim.Region region1 = claim.getRegion();

								region1.getChunks().forEach(claimChunk -> {
									Chunk chunk = ChunksManager.getFromLocation(world, claimChunk[0], claimChunk[1]);

									if (!ChunksManager.isChunkClaimed(chunk)) {
										ChunksManager.claimChunk(region.getUniqueId(), chunk);
									}
								});
							});
						});

						Logger.info("Imported a region; Name = " + region.getName() + ", ID = "
								+ region.getUniqueId().toString() + ", Owner = " + offlinePlayer.getName() + " ("
								+ offlinePlayer.getUniqueId() + ")");

						imported++;
					}
				}

				Map<String, String> replacements = new HashMap<>();
				replacements.put("{regions}", String.valueOf(imported));

				Messages.send(sender, 115, replacements);

				break;
			}
			default:
				Messages.send(sender, 113);
				break;
		}

		return true;
	}

	public ILandLord getLandLordInstance() {
		return (ILandLord) Bukkit.getServer().getPluginManager().getPlugin("Landlord");
	}

	public LandsIntegration getLandsInstance() {
		return new LandsIntegration(Homestead.getInstance());
	}
}

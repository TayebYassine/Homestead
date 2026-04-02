package tfagaming.projects.minecraft.homestead.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.other.UpkeepUtils;

public class PlaceholderAPI extends PlaceholderExpansion {
	public PlaceholderAPI() {
	}

	@Override
	public @NotNull String getIdentifier() {
		return "Homestead";
	}

	@Override
	public @NotNull String getAuthor() {
		return "TayebYassine";
	}

	@Override
	public @NotNull String getVersion() {
		return Homestead.getVersion();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String onPlaceholderRequest(Player player, @NotNull String params) {
		if (player == null) {
			return "";
		}

		Region region = TargetRegionSession.getRegion(player);
		Region currentRegion = ChunkManager.getRegionOwnsTheChunk(player.getLocation().getChunk());

		return switch (params.toLowerCase()) {
			case "region_bank" -> {
				if (region == null) {
					yield Resources.<ConfigFile>get(ResourceType.Config).getString("placeholderapi.default.region_bank");
				}

				yield Formatter.getBalance(region.getBank());
			}
			case "region_name" -> {
				if (region == null) {
					yield Resources.<ConfigFile>get(ResourceType.Config).getString("placeholderapi.default.region_name");
				}

				yield region.getName();
			}
			case "region_claimed_chunks" -> {
				if (region == null) {
					yield Resources.<ConfigFile>get(ResourceType.Config).getString("placeholderapi.default.region_claimed_chunks");
				}

				yield String.valueOf(region.getChunks().size());
			}
			case "region_max_chunks" -> {
				if (region == null) {
					yield Resources.<ConfigFile>get(ResourceType.Config).getString("placeholderapi.default.region_max_chunks");
				}

				yield String.valueOf(Limits.getPlayerLimit(player, Limits.LimitType.CHUNKS_PER_REGION));
			}
			case "region_trusted_members" -> {
				if (region == null) {
					yield Resources.<ConfigFile>get(ResourceType.Config).getString("placeholderapi.default.region_trusted_members");
				}

				yield String.valueOf(region.getMembers().size());
			}
			case "region_max_members" -> {
				if (region == null) {
					yield Resources.<ConfigFile>get(ResourceType.Config).getString("placeholderapi.default.region_max_members");
				}

				yield String.valueOf(Limits.getPlayerLimit(player, Limits.LimitType.MEMBERS_PER_REGION));
			}
			case "region_current" -> {
				if (currentRegion == null) {
					yield Resources.<ConfigFile>get(ResourceType.Config).getString("placeholderapi.default.region_current");
				}

				yield currentRegion.getName();
			}
			case "upkeep_amount" -> {
				if (region == null) {
					yield Resources.<ConfigFile>get(ResourceType.Config).getString("placeholderapi.default.upkeep_amount");
				}

				yield Formatter.getBalance(UpkeepUtils.getAmountToPay(region));
			}
			case "upkeep_at" -> {
				if (region == null) {
					yield Resources.<ConfigFile>get(ResourceType.Config).getString("placeholderapi.default.upkeep_at");
				}

				yield Formatter.getDate(region.getUpkeepAt());
			}
			case "war_name" -> {
				if (region == null || !WarManager.isRegionInWar(region.getUniqueId())) {
					yield Resources.<ConfigFile>get(ResourceType.Config).getString("placeholderapi.default.war_name");
				}

				yield WarManager.findWarByRegionId(region.getUniqueId()).getName();
			}
			case "war_prize" -> {
				if (region == null || !WarManager.isRegionInWar(region.getUniqueId())) {
					yield Resources.<ConfigFile>get(ResourceType.Config).getString("placeholderapi.default.war_prize");
				}

				yield Formatter.getBalance(WarManager.findWarByRegionId(region.getUniqueId()).getPrize());
			}
			default -> null;
		};
	}
}
package tfagaming.projects.minecraft.homestead.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.WarsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.other.UpkeepUtils;

public class PlaceholderAPI extends PlaceholderExpansion {
	private final Homestead plugin;

	public PlaceholderAPI(Homestead plugin) {
		this.plugin = plugin;
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
		return plugin.getDescription().getVersion();
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
		Region currentRegion = ChunksManager.getRegionOwnsTheChunk(player.getLocation().getChunk());

		return switch (params.toLowerCase()) {
			case "region_bank" -> {
				if (region == null) {
					yield Homestead.config.get("placeholderapi.default.region_bank");
				}

				yield Formatters.formatBalance(region.getBank());
			}
			case "region_name" -> {
				if (region == null) {
					yield Homestead.config.get("placeholderapi.default.region_name");
				}

				yield region.getName();
			}
			case "region_claimed_chunks" -> {
				if (region == null) {
					yield Homestead.config.get("placeholderapi.default.region_claimed_chunks");
				}

				yield String.valueOf(region.getChunks().size());
			}
			case "region_max_chunks" -> {
				if (region == null) {
					yield Homestead.config.get("placeholderapi.default.region_max_chunks");
				}

				yield String.valueOf(Limits.getPlayerLimit(player, Limits.LimitType.CHUNKS_PER_REGION));
			}
			case "region_trusted_members" -> {
				if (region == null) {
					yield Homestead.config.get("placeholderapi.default.region_trusted_members");
				}

				yield String.valueOf(region.getMembers().size());
			}
			case "region_max_members" -> {
				if (region == null) {
					yield Homestead.config.get("placeholderapi.default.region_max_members");
				}

				yield String.valueOf(Limits.getPlayerLimit(player, Limits.LimitType.MEMBERS_PER_REGION));
			}
			case "region_current" -> {
				if (currentRegion == null) {
					yield Homestead.config.get("placeholderapi.default.region_current");
				}

				yield currentRegion.getName();
			}
			case "upkeep_amount" -> {
				if (region == null) {
					yield Homestead.config.get("placeholderapi.default.upkeep_amount");
				}

				yield Formatters.formatBalance(UpkeepUtils.getAmountToPay(region));
			}
			case "upkeep_at" -> {
				if (region == null) {
					yield Homestead.config.get("placeholderapi.default.upkeep_at");
				}

				yield Formatters.formatDate(region.getUpkeepAt());
			}
			case "war_name" -> {
				if (region == null || !WarsManager.isRegionInWar(region.getUniqueId())) {
					yield Homestead.config.get("placeholderapi.default.war_name");
				}

				yield WarsManager.findWarByRegionId(region.getUniqueId()).getName();
			}
			case "war_prize" -> {
				if (region == null || !WarsManager.isRegionInWar(region.getUniqueId())) {
					yield Homestead.config.get("placeholderapi.default.war_prize");
				}

				yield Formatters.formatBalance(WarsManager.findWarByRegionId(region.getUniqueId()).getPrize());
			}
			default -> null;
		};
	}
}
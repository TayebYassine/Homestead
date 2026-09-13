package me.tayebyassine.homestead.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.WarManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionMember;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.minecraft.economy.UpkeepUtility;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PlaceholderAPI extends PlaceholderExpansion {
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
        Location location = player.getLocation();
        Chunk chunk = location.getChunk();
        Region currentRegion = ChunkManager.getRegionOwnsTheChunk(chunk);

        return switch (params.toLowerCase()) {
            case "region_bank" -> {
                if (region == null) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("region_bank");
                }

                yield Formatter.getBalance(region.getBank());
            }
            case "region_name" -> {
                if (region == null) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("region_name");
                }

                yield region.getName();
            }
            case "region_rank" -> {
                if (region == null) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("region_rank");
                }

                yield String.valueOf(RegionManager.getGlobalRank(region.getUniqueId()));
            }
            case "region_claimed_chunks" -> {
                if (region == null) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("region_claimed_chunks");
                }

                yield String.valueOf(ChunkManager.getChunksOfRegion(region).size());
            }
            case "region_max_chunks" -> {
                if (region == null) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("region_max_chunks");
                }

                yield String.valueOf(Limits.getRegionLimit(region, Limits.LimitType.CHUNKS_PER_REGION));
            }
            case "region_trusted_members" -> {
                if (region == null) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("region_trusted_members");
                }

                yield String.valueOf(MemberManager.getMembersOfRegion(region).size());
            }
            case "region_max_members" -> {
                if (region == null) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("region_max_members");
                }

                yield String.valueOf(Limits.getRegionLimit(region, Limits.LimitType.MEMBERS_PER_REGION));
            }
            case "region_subareas" -> {
                if (region == null || !Resources.<RegionsFile>get(ResourceType.Regions).isSubAreasEnabled()) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("region_subareas");
                }

                yield String.valueOf(MemberManager.getMembersOfRegion(region).size());
            }
            case "region_max_subareas" -> {
                if (region == null || !Resources.<RegionsFile>get(ResourceType.Regions).isSubAreasEnabled()) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("region_max_subareas");
                }

                yield String.valueOf(Limits.getRegionLimit(region, Limits.LimitType.SUBAREAS_PER_REGION));
            }
            case "region_current" -> {
                if (currentRegion == null) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("region_current");
                }

                yield currentRegion.getName();
            }
            case "upkeep_amount" -> {
                if (region == null || !Resources.<RegionsFile>get(ResourceType.Regions).isUpkeepEnabled()) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("upkeep_amount");
                }

                yield Formatter.getBalance(UpkeepUtility.getAmountToPay(region));
            }
            case "upkeep_at" -> {
                if (region == null || !Resources.<RegionsFile>get(ResourceType.Regions).isUpkeepEnabled()) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("upkeep_at");
                }

                yield Formatter.getRemainingTime(region.getUpkeepAt());
            }
            case "tax_amount" -> {
                if (region == null || !Resources.<RegionsFile>get(ResourceType.Regions).isTaxesEnabled()) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("tax_amount");
                }

                yield Formatter.getBalance(region.getTaxes());
            }
            case "tax_at" -> {
                if (region == null || !Resources.<RegionsFile>get(ResourceType.Regions).isTaxesEnabled()) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("tax_at_date");
                }

                RegionMember member = MemberManager.getMemberOfRegion(region, player);

                if (member == null) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("tax_at_date");
                }

                yield Formatter.getDuration(member.getTaxesAt());
            }
            case "war_name" -> {
                if (region == null || !WarManager.isRegionInWar(region.getUniqueId()) || !Resources.<RegionsFile>get(ResourceType.Regions).isWarsEnabled()) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("war_name");
                }

                yield WarManager.findWarByRegion(region.getUniqueId()).getName();
            }
            case "war_prize" -> {
                if (region == null || !WarManager.isRegionInWar(region.getUniqueId()) || !Resources.<RegionsFile>get(ResourceType.Regions).isWarsEnabled()) {
                    yield Resources.<ConfigFile>get(ResourceType.Config).getPlaceholderDefault("war_prize");
                }

                yield Formatter.getBalance(WarManager.findWarByRegion(region.getUniqueId()).getPrize());
            }
            default -> null;
        };
    }
}
package me.tayebyassine.homestead.borders;

import me.tayebyassine.homestead.borders.blocks.BorderBlockRenderer;
import me.tayebyassine.homestead.borders.glow.ChunkGlowSpawner;
import me.tayebyassine.homestead.borders.glow.SelectedAreaGlowSpawner;
import me.tayebyassine.homestead.borders.particles.ChunkParticlesSpawner;
import me.tayebyassine.homestead.borders.particles.SelectedAreaParticlesSpawner;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Central manager for region border visualization.
 * <p>
 * Provides a unified API for showing/hiding region borders using different
 * visualization methods: particles, fake blocks, or glowing entities.
 * </p>
 *
 * @see BorderBlockRenderer
 * @see ChunkParticlesSpawner
 * @see ChunkGlowSpawner
 * @see SelectedAreaParticlesSpawner
 * @see SelectedAreaGlowSpawner
 */
public final class ChunkBorder {
    private ChunkBorder() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Gets the currently configured border visualization type.
     *
     * @return the border type, or null if borders are disabled
     */
    public static BorderType getBorderType() {
        RegionsFile config = Resources.get(ResourceType.Regions);

        if (!config.isBordersEnabled()) {
            return null;
        }

        String type = config.getString("borders.type");

        return switch (type) {
            case "particles" -> BorderType.PARTICLES;
            case "blocks" -> BorderType.BLOCKS;
            case "glow" -> BorderType.GLOW;
            default -> null;
        };
    }

    /**
     * Gets the configured block material for block-based borders.
     *
     * @return the material, defaulting to GOLD_BLOCK if invalid
     */
    public static Material getBlockType() {
        String blockType = Resources.<RegionsFile>get(ResourceType.Regions).getString("borders.block-type");
        Material material = Material.getMaterial(blockType);
        return material != null ? material : Material.GOLD_BLOCK;
    }

    /**
     * Stops all border visualizations for a player.
     *
     * @param player the player to stop borders for
     */
    public static void stop(Player player) {
        if (player == null) return;
        BorderBlockRenderer.removeAll(player);
        ChunkParticlesSpawner.cancelTask(player);
        SelectedAreaParticlesSpawner.cancelTask(player);
        ChunkGlowSpawner.cancelTask(player);
        SelectedAreaGlowSpawner.cancelTask(player);
    }

    /**
     * Shows the appropriate border for the player's current location.
     * <p>
     * Determines the region and subarea at the player's position and starts
     * the configured visualization method.
     * </p>
     *
     * @param player the player to show borders to
     */
    public static void show(Player player) {
        if (player == null) return;

        Region region = ChunkManager.getRegionOwnsTheChunk(player.getLocation().getChunk());
        SubArea subArea = null;

        if (region != null) {
            subArea = SubAreaManager.findSubAreaHasLocationInside(player.getLocation());
        }

        BorderType borderType = getBorderType();

        switch (borderType) {
            case PARTICLES -> showParticles(player, region, subArea);
            case BLOCKS -> showBlocks(player, region, subArea);
            case GLOW -> showGlow(player, region, subArea);
            case null, default -> {
            }
        }
    }

    private static void showParticles(Player player, Region region, SubArea subArea) {
        if (region == null) {
            new ChunkParticlesSpawner(player);
        } else if (subArea != null) {
            new SelectedAreaParticlesSpawner(player, subArea.getPoint1(), subArea.getPoint2());
        } else {
            new ChunkParticlesSpawner(player);
        }
    }

    private static void showBlocks(Player player, Region region, SubArea subArea) {
        BorderBlockRenderer.removeAll(player);

        if (region != null) {
            if (subArea != null) {
                new SelectedAreaParticlesSpawner(player, subArea.getPoint1(), subArea.getPoint2());
            } else {
                BorderBlockRenderer.show(player, region);
            }
        }
    }

    private static void showGlow(Player player, Region region, SubArea subArea) {
        if (region == null) {
            new ChunkGlowSpawner(player);
        } else if (subArea != null) {
            new SelectedAreaGlowSpawner(player, subArea.getPoint1(), subArea.getPoint2());
        } else {
            new ChunkGlowSpawner(player);
        }
    }

    /**
     * Enum representing available border visualization types.
     */
    public enum BorderType {
        /**
         * Dust particles along chunk edges.
         */
        PARTICLES,

        /**
         * Fake block changes sent to player client.
         */
        BLOCKS,

        /**
         * Glowing entities (invisible markers) at border positions.
         */
        GLOW
    }
}
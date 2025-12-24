package tfagaming.projects.minecraft.homestead.tools.minecraft.chunks;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.borders.BorderBlockRenderer;
import tfagaming.projects.minecraft.homestead.borders.ChunkParticlesSpawner;
import tfagaming.projects.minecraft.homestead.borders.SelectedAreaParticlesSpawner;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableSubArea;

public class ChunkBorder {
    public static BorderType getMethod() {
        boolean isEnabled = Homestead.config.get("borders.enabled");

        if (!isEnabled) {
            return null;
        }

        String type = Homestead.config.get("borders.type");

        return switch (type) {
            case "particles" -> BorderType.PARTICLES;
            case "blocks" -> BorderType.BLOCKS;
            default -> null;
        };
    }

    public static void stop(Player player) {
        BorderBlockRenderer.removeAll(player);
        ChunkParticlesSpawner.cancelTask(player);
        SelectedAreaParticlesSpawner.cancelTask(player);
    }

    public static void show(Player player) {
        Region region = ChunksManager.getRegionOwnsTheChunk(player.getLocation().getChunk());
        SerializableSubArea subArea = null;

        if (region != null) {
            subArea = region.findSubAreaHasLocationInside(player.getLocation());
        }

        stop(player);

        BorderType borderType = getMethod();

        switch (borderType) {
            case PARTICLES: {
                if (region == null) {
                    new ChunkParticlesSpawner(player);
                } else {
                    if (subArea != null) {
                        new SelectedAreaParticlesSpawner(player, subArea.getFirstPoint(), subArea.getSecondPoint());
                    } else {
                        new ChunkParticlesSpawner(player);
                    }
                }

                break;
            }

            case BLOCKS: {
                if (region != null) {
                    if (subArea != null) {
                        new SelectedAreaParticlesSpawner(player, subArea.getFirstPoint(), subArea.getSecondPoint());
                    } else {
                        BorderBlockRenderer.show(player, region);
                    }
                }

                break;
            }

            case null: {
                break;
            }
        }
    }

    public enum BorderType {
        PARTICLES,
        BLOCKS
    }
}

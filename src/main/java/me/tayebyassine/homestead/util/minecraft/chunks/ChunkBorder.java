package me.tayebyassine.homestead.util.minecraft.chunks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.borders.BorderBlockRenderer;
import me.tayebyassine.homestead.borders.ChunkParticlesSpawner;
import me.tayebyassine.homestead.borders.SelectedAreaParticlesSpawner;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;


public final class ChunkBorder {
	private ChunkBorder() {
	}

	public static BorderType getMethod() {
		boolean isEnabled = Resources.<RegionsFile>get(ResourceType.Regions).isBordersEnabled();

		if (!isEnabled) {
			return null;
		}

		String type = Resources.<RegionsFile>get(ResourceType.Regions).getString("borders.type");

		return switch (type) {
			case "particles" -> BorderType.PARTICLES;
			case "blocks" -> BorderType.BLOCKS;
			default -> null;
		};
	}

	public static Material getBlockType() {
		String blockType = Resources.<RegionsFile>get(ResourceType.Regions).getString("borders.block-type");

		return Material.getMaterial(blockType) == null ? Material.GOLD_BLOCK : Material.getMaterial(blockType);
	}

	public static void stop(Player player) {
		BorderBlockRenderer.removeAll(player);
		ChunkParticlesSpawner.cancelTask(player);
		SelectedAreaParticlesSpawner.cancelTask(player);
	}

	public static void show(Player player) {
		Region region = ChunkManager.getRegionOwnsTheChunk(player.getLocation().getChunk());
		SubArea subArea = null;

		if (region != null) {
			subArea = SubAreaManager.findSubAreaHasLocationInside(player.getLocation());
		}

		BorderType borderType = getMethod();

		switch (borderType) {
			case PARTICLES: {
				if (region == null) {
					new ChunkParticlesSpawner(player);
				} else {
					if (subArea != null) {
						new SelectedAreaParticlesSpawner(player, subArea.getPoint1(), subArea.getPoint2());
					} else {
						new ChunkParticlesSpawner(player);
					}
				}

				break;
			}

			case BLOCKS: {
				BorderBlockRenderer.removeAll(player);

				if (region != null) {
					if (subArea != null) {
						new SelectedAreaParticlesSpawner(player, subArea.getPoint1(), subArea.getPoint2());
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

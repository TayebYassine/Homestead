package tfagaming.projects.minecraft.homestead.borders;

import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class BorderBlockRenderer {

	public static void show(Player player, Region region) {
		removeAll(player);                                       // clean old
		Set<FakeBorderRegistry.FakeBorderBlock> batch = Sets.newHashSet();

		World world = player.getWorld();
		for (SerializableChunk sc : region.getChunks()) {
			if (!world.getName().equals(sc.getWorldName())) continue;

			int cx = sc.getX();
			int cz = sc.getZ();

			if (!isChunkInRegion(region, world, cx, cz - 1)) {
				addBorderColumn(batch, player, region, world, cx, cz, Direction.NORTH);
			}

			if (!isChunkInRegion(region, world, cx, cz + 1)) {
				addBorderColumn(batch, player, region, world, cx, cz, Direction.SOUTH);
			}

			if (!isChunkInRegion(region, world, cx - 1, cz)) {
				addBorderColumn(batch, player, region, world, cx, cz, Direction.WEST);
			}

			if (!isChunkInRegion(region, world, cx + 1, cz)) {
				addBorderColumn(batch, player, region, world, cx, cz, Direction.EAST);
			}
		}

		batch.forEach(FakeBorderRegistry::add);
	}

	public static void removeAll(Player player) {
		UUID viewer = player.getUniqueId();
		List<FakeBorderRegistry.FakeBorderBlock> remove = new ArrayList<>();

		FakeBorderRegistry.REGION_MAP.values()
				.forEach(set -> set.forEach(b -> {
					if (b.viewerUUID().equals(viewer)) remove.add(b);
				}));

		remove.forEach(b -> player.sendBlockChange(b.loc(), b.originalData()));

		remove.forEach(b -> {
			FakeBorderRegistry.LOC_MAP.remove(b.loc());
			Set<FakeBorderRegistry.FakeBorderBlock> regSet =
					FakeBorderRegistry.REGION_MAP.get(b.regionUUID());
			if (regSet != null) regSet.remove(b);
		});
	}

	public static void removeRegion(UUID regionUUID) {
		Set<FakeBorderRegistry.FakeBorderBlock> blocks =
				FakeBorderRegistry.removeRegion(regionUUID);
		blocks.forEach(b -> {
			Player viewer = Bukkit.getPlayer(b.viewerUUID());
			if (viewer != null && viewer.isOnline())
				viewer.sendBlockChange(b.loc(), b.originalData());
		});
	}

	private static boolean isChunkInRegion(Region region, World world, int cx, int cz) {
		return region.getChunks().contains(new SerializableChunk(world.getName(), cx, cz));
	}

	private static void addBorderColumn(Set<FakeBorderRegistry.FakeBorderBlock> batch,
										Player player, Region region,
										World world, int cx, int cz, Direction dir) {

		int baseX = cx << 4;
		int baseZ = cz << 4;

		int stepX = 0, stepZ = 0, startX = 0, startZ = 0;

		Material borderMat = ChunkBorder.getBlockType();

		switch (dir) {
			case NORTH -> {
				startX = baseX;
				startZ = baseZ;
				stepX = 1;
			} // z = const
			case SOUTH -> {
				startX = baseX;
				startZ = baseZ + 15;
				stepX = 1;
			} // z = const
			case WEST -> {
				startX = baseX;
				startZ = baseZ;
				stepZ = 1;
			} // x = const
			case EAST -> {
				startX = baseX + 15;
				startZ = baseZ;
				stepZ = 1;
			} // x = const
		}

		for (int i = 0; i < 16; i++) {
			int x = startX + i * stepX;
			int z = startZ + i * stepZ;
			int y = world.getHighestBlockYAt(x, z);

			Location loc = new Location(world, x, y, z);
			BlockData original = loc.getBlock().getBlockData();
			BlockData fake = borderMat.createBlockData();

			player.sendBlockChange(loc, fake);
			batch.add(new FakeBorderRegistry.FakeBorderBlock(
					loc, original, region.getUniqueId(), player.getUniqueId()));
		}
	}

	private enum Direction {NORTH, SOUTH, EAST, WEST}
}
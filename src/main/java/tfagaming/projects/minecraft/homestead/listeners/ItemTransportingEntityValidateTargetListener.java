package tfagaming.projects.minecraft.homestead.listeners;

import io.papermc.paper.event.entity.ItemTransportingEntityValidateTargetEvent;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.models.Region;


public final class ItemTransportingEntityValidateTargetListener implements Listener {
	public static boolean isClassFound() {
		try {
			Class.forName("io.papermc.paper.event.entity.ItemTransportingEntityValidateTargetEvent");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemTransportValidate(ItemTransportingEntityValidateTargetEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof CopperGolem) {
			Block targetBlock = event.getBlock();

			Chunk chunk = targetBlock.getLocation().getChunk();

			if (ChunkManager.isChunkClaimed(chunk)) {
				Region region = ChunkManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.ENTITY_GRIEFING)) {
					event.setAllowed(false);
				}
			}
		}
	}
}

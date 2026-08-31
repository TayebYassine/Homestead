package me.tayebyassine.homestead.listeners.entities;

import io.papermc.paper.event.entity.ItemTransportingEntityValidateTargetEvent;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.flags.WorldRules;
import me.tayebyassine.homestead.listeners.util.CopperGolemTracker;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.models.Region;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Paper-only listener that governs whether Copper Golems may pick up / move items across region
 * boundaries using the {@link me.tayebyassine.homestead.flags.WorldFlag#ENTITY_GRIEFING} flag.
 */
public final class ItemTransportingEntityValidateTargetListener implements Listener {

    /**
     * @return {@code true} if the Paper {@code ItemTransportingEntityValidateTargetEvent} is available on the server.
     */
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

        if (!(entity instanceof CopperGolem golem)) {
            return;
        }

        Block targetBlock = event.getBlock();
        Chunk targetChunk = targetBlock.getLocation().getChunk();

        Region targetRegion = ChunkManager.getRegionOwnsTheChunk(targetChunk);

        Long spawnRegionId = CopperGolemTracker.getSpawnRegionId(golem);

        if (targetRegion != null) {
            Long targetRegionId = targetRegion.getUniqueId();

            if (spawnRegionId != null && spawnRegionId.equals(targetRegionId)) {
                return;
            }

            if (!targetRegion.isWorldFlagSet(WorldFlag.ENTITY_GRIEFING.getBitmask())) {
                event.setAllowed(false);
            }
        } else {
            if (spawnRegionId == null && !WorldRules.isWorldFlagAllowed(targetChunk.getWorld(), WorldFlag.ENTITY_GRIEFING.getBitmask())) {
                entity.remove();
            }
        }
    }
}

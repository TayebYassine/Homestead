package me.tayebyassine.homestead.listeners.protection;

import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.flags.WorldRules;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.models.Region;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Handles region protection for block-related world events: placement, destruction,
 * fire, growth, pistons, dispensers, liquids, explosions, and weather.
 */
public final class BlockProtectionHandler extends ProtectionHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(event.getPlayer(), block.getChunk(), block.getLocation(),
                PlayerFlag.PLACE_BLOCKS, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(event.getPlayer(), block.getChunk(), block.getLocation(),
                PlayerFlag.BREAK_BLOCKS, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        Chunk chunk = location.getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        if (player == null) {
            checkWorldFlag(chunk, WorldFlag.FIRE_SPREAD, cancel);
            return;
        }

        checkPlayerFlag(player, chunk, location, PlayerFlag.IGNITE, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        BlockState newState = event.getNewState();
        Block block = event.getBlock();
        Block source = event.getSource();
        Chunk chunk = block.getLocation().getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        if (newState.getType() == Material.FIRE) {
            checkWorldFlag(chunk, WorldFlag.FIRE_SPREAD, cancel);
        } else if (source.getType() == Material.GRASS_BLOCK || source.getType() == Material.MYCELIUM) {
            checkWorldFlag(chunk, WorldFlag.GRASS_GROWTH, cancel);
        } else if (source.getType() == Material.SCULK_CATALYST) {
            checkWorldFlag(chunk, WorldFlag.SCULK_SPREAD, cancel);
        } else {
            checkWorldFlag(chunk, WorldFlag.PLANT_GROWTH, cancel);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        Chunk chunk = event.getBlock().getLocation().getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        checkWorldFlag(chunk, WorldFlag.PLANT_GROWTH, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Chunk chunk = event.getBlock().getLocation().getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        checkWorldFlag(chunk, WorldFlag.FIRE_SPREAD, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        Material blockType = event.getBlock().getType();
        Chunk chunk = event.getBlock().getLocation().getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        if (blockType == Material.SNOW) {
            checkWorldFlag(chunk, WorldFlag.SNOW_MELTING, cancel);
        } else if (blockType == Material.ICE) {
            checkWorldFlag(chunk, WorldFlag.ICE_MELTING, cancel);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        Chunk chunk = event.getBlock().getLocation().getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        checkWorldFlag(chunk, WorldFlag.LEAVES_DECAY, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTreeGrow(StructureGrowEvent event) {
        Chunk chunk = event.getLocation().getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        checkWorldFlag(chunk, WorldFlag.PLANT_GROWTH, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLiquidFlow(BlockFromToEvent event) {
        Chunk fromChunk = event.getBlock().getChunk();
        Chunk toChunk = event.getToBlock().getChunk();

        if (fromChunk.equals(toChunk)) {
            return;
        }

        Region fromRegion = ChunkManager.getRegionOwnsTheChunk(fromChunk);
        Region toRegion = ChunkManager.getRegionOwnsTheChunk(toChunk);

        if (fromRegion == null && toRegion == null) {
            return;
        }

        if (fromRegion != null && toRegion != null && fromRegion.getUniqueId() == toRegion.getUniqueId()) {
            return;
        }

        if (toRegion != null && !toRegion.isWorldFlagSet(WorldFlag.LIQUID_FLOW)) {
            event.setCancelled(true);
            return;
        }

        if (fromRegion != null && !fromRegion.isWorldFlagSet(WorldFlag.LIQUID_FLOW)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Block piston = event.getBlock();
        @SuppressWarnings({"rawtypes", "unchecked"})
        List<Block> affectedBlocks = new ArrayList(event.getBlocks());
        BlockFace direction = event.getDirection();

        if (!affectedBlocks.isEmpty()) {
            affectedBlocks.add(piston.getRelative(direction));
        }

        if (!canPistonMoveBlock(affectedBlocks, direction, piston.getLocation().getChunk(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Block piston = event.getBlock();
        @SuppressWarnings({"rawtypes", "unchecked"})
        List<Block> affectedBlocks = new ArrayList(event.getBlocks());
        BlockFace direction = event.getDirection();

        if (event.isSticky() && !affectedBlocks.isEmpty()) {
            affectedBlocks.add(piston.getRelative(direction));
        }

        if (!canPistonMoveBlock(affectedBlocks, direction, piston.getLocation().getChunk(), true)) {
            event.setCancelled(true);
        }
    }

    private boolean canPistonMoveBlock(List<Block> blocks, BlockFace direction, Chunk pistonChunk,
                                       boolean retractOrNot) {
        @SuppressWarnings("rawtypes")
        Iterator var5;
        Block block;
        Chunk chunk;

        if (retractOrNot) {
            var5 = blocks.iterator();

            while (var5.hasNext()) {
                block = (Block) var5.next();
                chunk = block.getLocation().getChunk();

                if (!chunk.equals(pistonChunk) && ChunkManager.isChunkClaimed(chunk)) {
                    Region pistonChunkRegion = ChunkManager.getRegionOwnsTheChunk(pistonChunk);
                    UUID pistonChunkOwner = pistonChunkRegion == null ? null : pistonChunkRegion.getOwnerId();
                    Region targetRegion = ChunkManager.getRegionOwnsTheChunk(chunk);
                    UUID targetChunkOwner = targetRegion == null ? null : targetRegion.getOwnerId();

                    if (pistonChunkRegion != null && pistonChunkOwner != null && pistonChunkOwner.equals(targetChunkOwner)) {
                        return true;
                    }

                    Region region = ChunkManager.getRegionOwnsTheChunk(chunk);

                    if (region != null && !region.isWorldFlagSet(WorldFlag.WILDERNESS_PISTONS)) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            var5 = blocks.iterator();

            while (var5.hasNext()) {
                block = (Block) var5.next();
                chunk = block.getRelative(direction).getLocation().getChunk();

                if (!chunk.equals(pistonChunk) && ChunkManager.isChunkClaimed(chunk)) {
                    Region pistonChunkRegion = ChunkManager.getRegionOwnsTheChunk(pistonChunk);
                    UUID pistonChunkOwner = pistonChunkRegion == null ? null : pistonChunkRegion.getOwnerId();
                    Region targetRegion = ChunkManager.getRegionOwnsTheChunk(chunk);
                    UUID targetChunkOwner = targetRegion == null ? null : targetRegion.getOwnerId();

                    if (pistonChunkRegion != null && pistonChunkOwner != null && pistonChunkOwner.equals(targetChunkOwner)) {
                        return true;
                    }

                    Region region = ChunkManager.getRegionOwnsTheChunk(chunk);

                    if (region != null && !region.isWorldFlagSet(WorldFlag.WILDERNESS_PISTONS)) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        BlockData blockdata = event.getBlock().getBlockData();
        Chunk targetChunk = block.getRelative(((Directional) blockdata).getFacing()).getLocation().getChunk();

        if (!block.getLocation().getChunk().equals(targetChunk)) {
            if (ChunkManager.isChunkClaimed(targetChunk)) {
                Region dispenserChunkRegion = ChunkManager.getRegionOwnsTheChunk(block.getLocation().getChunk());
                UUID dispenserChunkOwner = dispenserChunkRegion == null ? null : dispenserChunkRegion.getOwnerId();
                Region targetRegion = ChunkManager.getRegionOwnsTheChunk(targetChunk);
                UUID targetChunkOwner = targetRegion == null ? null : targetRegion.getOwnerId();

                if (dispenserChunkRegion != null && dispenserChunkOwner != null && dispenserChunkOwner.equals(targetChunkOwner)) {
                    return;
                }

                Region region = ChunkManager.getRegionOwnsTheChunk(targetChunk);

                if (region != null && !region.isWorldFlagSet(WorldFlag.WILDERNESS_DISPENSERS)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        Chunk chunk = location.getChunk();

        if (ChunkManager.isChunkClaimed(chunk)) {
            Region region = ChunkManager.getRegionOwnsTheChunk(chunk);

            if (region != null && !region.isWorldFlagSet(WorldFlag.EXPLOSION_DAMAGE)) {
                event.setCancelled(true);
            }
        } else {
            if (!WorldRules.isWorldFlagAllowed(chunk.getWorld(), WorldFlag.EXPLOSION_DAMAGE)) {
                event.setCancelled(true);
                return;
            }

            List<Block> allowedBlocks = new ArrayList<>();

            for (Block b : event.blockList()) {
                if (!ChunkManager.isChunkClaimed(b.getLocation().getChunk())) {
                    allowedBlocks.add(b);
                }
            }

            event.blockList().clear();
            event.blockList().addAll(allowedBlocks);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSnowGolemTrail(EntityBlockFormEvent event) {
        if (!(event.getEntity() instanceof Snowman)) {
            return;
        }

        Chunk chunk = event.getBlock().getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        checkWorldFlag(chunk, WorldFlag.SNOWMAN_TRAILS, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWeatherSnowForm(EntityBlockFormEvent event) {
        if (event.getNewState().getType() == Material.SNOW) {
            Chunk chunk = event.getBlock().getChunk();
            Runnable cancel = () -> event.setCancelled(true);

            checkWorldFlag(chunk, WorldFlag.WEATHER_SNOW, cancel);
        }
    }
}

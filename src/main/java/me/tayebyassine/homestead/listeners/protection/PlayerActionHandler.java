package me.tayebyassine.homestead.listeners.protection;

import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.models.Region;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Fence;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Handles region protection for player-initiated actions: block interactions,
 * item usage, bucket operations, vehicles, elytra, fall damage, potions, and more.
 */
public final class PlayerActionHandler extends ProtectionHandler implements Listener {

    /**
     * Handles most player interactions with blocks and placeable items in (claimed) chunks.
     * Uses Bukkit tags where available and centralizes the permission gating into
     * {@link #checkPlayerFlag}.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block clicked = event.getClickedBlock();
        final Location location = (clicked != null ? clicked.getLocation() : player.getLocation());
        final Chunk chunk = location.getChunk();
        final Runnable cancel = () -> event.setCancelled(true);
        final ItemStack item = event.getItem();

        if (item != null) {
            final Material itemType = item.getType();
            final String itn = itemType.name();

            final boolean placeSpawnItem =
                    itn.contains("BOAT") ||
                            itn.contains("ARMOR_STAND") ||
                            itn.contains("MINECART") ||
                            itn.contains("PAINTING") ||
                            isCushion(itn) ||
                            itemType == Material.BONE_MEAL ||
                            itemType == Material.ITEM_FRAME ||
                            itemType == Material.GLOW_ITEM_FRAME;

            if (placeSpawnItem) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.PLACE_BLOCKS, cancel);
                return;
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clicked != null) {
            final Material type = clicked.getType();

            if (isShulkerBox(type)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.CONTAINERS, cancel);
            } else if (isAnySign(type)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.PLACE_BLOCKS, cancel);
            } else if (isContainerLike(type)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.CONTAINERS, cancel);
            } else if (isAnvil(type)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.USE_ANVIL, cancel);
            } else if (Tag.TRAPDOORS.isTagged(type)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.TRAP_DOORS, cancel);
            } else if (Tag.DOORS.isTagged(type) || type.name().contains("DOOR")) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.DOORS, cancel);
            } else if (isArchaeologyBlockWithBrush(type, player)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.BREAK_BLOCKS, cancel);
            } else if (Tag.BUTTONS.isTagged(type)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.BUTTONS, cancel);
            } else if (type.name().contains("FENCE_GATE")) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.FENCE_GATES, cancel);
            } else if (isSmallInteractable(type)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.GENERAL_INTERACTION, cancel);
            } else if (isLecternOrVaultWithKey(type, player)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.CONTAINERS, cancel);
            } else if (type.name().endsWith("_BED")) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.SLEEP, cancel);
            } else if (type == Material.LEVER) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.LEVERS, cancel);
            } else if (type == Material.BELL) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.USE_BELLS, cancel);
            } else if (isRedstoneInteraction(type)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.REDSTONE, cancel);
            }
            return;
        }

        if (event.getAction() == Action.PHYSICAL && clicked != null) {
            final Material type = clicked.getType();

            if (Tag.PRESSURE_PLATES.isTagged(type)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.PRESSURE_PLATES, cancel);
            } else if (type == Material.TRIPWIRE) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.TRIGGER_TRIPWIRE, cancel);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Block relative = event.getBlockClicked().getRelative(event.getBlockFace());
        Location location = relative.getLocation();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.PLACE_BLOCKS, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Block relative = event.getBlockClicked().getRelative(event.getBlockFace());
        Location location = relative.getLocation();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.BREAK_BLOCKS, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerExtinguishFire(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block targetBlock = event.getPlayer().getTargetBlock(null, 5);
        if (targetBlock.getType() != Material.FIRE) {
            return;
        }

        Location location = targetBlock.getLocation();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.IGNITE, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTrampleBlock(PlayerInteractEvent event) {
        Block blockClicked = event.getClickedBlock();

        if (event.getAction() != Action.PHYSICAL || blockClicked == null) {
            return;
        }

        if (List.of(Material.FARMLAND, Material.TURTLE_EGG).contains(blockClicked.getType())) {
            Location location = blockClicked.getLocation();
            Runnable cancel = () -> event.setCancelled(true);

            checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                    PlayerFlag.BLOCK_TRAMPLING, cancel);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHarvestCrop(PlayerInteractEvent event) {
        Block blockClicked = event.getClickedBlock();

        if (blockClicked == null || !isCropBlock(blockClicked)) {
            return;
        }

        Location location = blockClicked.getLocation();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.HARVEST_CROPS, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPlaceSpawnEgg(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Block blockClicked = event.getClickedBlock();

        if (item == null || blockClicked == null
                || (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)) {
            return;
        }

        if (item.getType().name().endsWith("_SPAWN_EGG")) {
            Location location = blockClicked.getLocation();
            Runnable cancel = () -> event.setCancelled(true);

            checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                    PlayerFlag.SPAWN_ENTITIES, cancel);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractThrowThrowable(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        Material type = item.getType();
        PlayerFlag flag;

        if (type == Material.ENDER_PEARL) {
            flag = PlayerFlag.TELEPORT;
        } else if (type == Material.SPLASH_POTION || type == Material.LINGERING_POTION) {
            flag = PlayerFlag.THROW_POTIONS;
        } else {
            return;
        }

        Player player = event.getPlayer();
        Location location = player.getLocation();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(player, location.getChunk(), location, flag, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEatChorusFruit(PlayerItemConsumeEvent event) {
        if (!event.getItem().getType().equals(Material.CHORUS_FRUIT)) {
            return;
        }

        Player player = event.getPlayer();
        Location location = player.getLocation();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(player, location.getChunk(), location,
                PlayerFlag.TELEPORT, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(player, location.getChunk(), location,
                PlayerFlag.PICKUP_ITEMS, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Location location = event.getItem().getLocation();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(player, location.getChunk(), location,
                PlayerFlag.PICKUP_ITEMS, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player player) {
            Location location = event.getVehicle().getLocation();
            Runnable cancel = () -> event.setCancelled(true);

            checkPlayerFlag(player, location.getChunk(), location,
                    PlayerFlag.VEHICLES, cancel);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player player) {
            Location location = event.getVehicle().getLocation();
            Runnable cancel = () -> event.setCancelled(true);

            checkPlayerFlag(player, location.getChunk(), location,
                    PlayerFlag.VEHICLES, cancel);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart)) {
            return;
        }

        org.bukkit.Chunk fromChunk = event.getFrom().getChunk();
        org.bukkit.Chunk toChunk = event.getTo().getChunk();

        if (fromChunk.equals(toChunk)) {
            return;
        }

        if (ChunkManager.isChunkClaimed(toChunk)) {
            Region fromRegion = ChunkManager.getRegionOwnsTheChunk(fromChunk);
            Region toRegion = ChunkManager.getRegionOwnsTheChunk(toChunk);

            if (fromRegion == null) {
                if (toRegion != null && !toRegion.isWorldFlagSet(WorldFlag.WILDERNESS_MINECARTS)) {
                    event.getVehicle().remove();
                }
            } else if (toRegion != null && fromRegion.getUniqueId() != toRegion.getUniqueId()) {
                if (!toRegion.isWorldFlagSet(WorldFlag.WILDERNESS_MINECARTS)) {
                    event.getVehicle().remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeashEvent(PlayerLeashEntityEvent event) {
        Entity entity = event.getEntity();
        Location location = entity.getLocation();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.INTERACT_ENTITIES, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        Location location = event.getEntity().getLocation();
        Block block = location.getBlock();
        if (!(block.getBlockData() instanceof Fence)) {
            return;
        }

        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.INTERACT_ENTITIES, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerFrostWalkerEnchantedBootsUsage(EntityBlockFormEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        Location location = event.getBlock().getLocation();
        Chunk chunk = location.getChunk();
        EntityEquipment equipment = player.getEquipment();

        if (equipment == null) return;

        ItemStack boots = equipment.getBoots();

        if (boots != null && boots.getEnchantments().containsKey(Enchantment.FROST_WALKER)) {
            Runnable cancel = () -> event.setCancelled(true);

            checkPlayerFlag(player, chunk, location, PlayerFlag.FROST_WALKER, cancel);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerFallDamage(EntityDamageEvent event) {
        DamageCause cause = event.getCause();
        if (cause != DamageCause.FALL && cause != DamageCause.FLY_INTO_WALL) {
            return;
        }

        if (event.getEntity() instanceof Player player) {
            Location location = player.getLocation();
            Runnable cancel = () -> event.setCancelled(true);

            checkPlayerFlag(player, location.getChunk(), location,
                    PlayerFlag.TAKE_FALL_DAMAGE, cancel);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBrushBlock(BlockDropItemEvent event) {
        Material type = event.getBlockState().getType();

        if (isArchaeologyBlock(type)) {
            Location location = event.getBlock().getLocation();
            Runnable cancel = () -> event.setCancelled(true);

            checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                    PlayerFlag.BREAK_BLOCKS, cancel);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPunchFrame(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof ItemFrame && event.getDamager() instanceof Player player) {
            Location location = entity.getLocation();
            Runnable cancel = () -> event.setCancelled(true);

            checkPlayerFlag(player, location.getChunk(), location,
                    PlayerFlag.BREAK_BLOCKS, cancel);
        }
    }
}

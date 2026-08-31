package me.tayebyassine.homestead.listeners.protection;

import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.flags.WorldRules;
import me.tayebyassine.homestead.listeners.util.CopperGolemTracker;
import me.tayebyassine.homestead.listeners.util.Explosives;
import me.tayebyassine.homestead.listeners.util.RegionProtection;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.FlagsFile;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Fence;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central listener that enforces Homestead region and wilderness protection rules.
 *
 * <p>Every handler funnels permission checks through two small helpers:
 * <ul>
 *     <li>{@link #checkPlayerFlag(Player, Chunk, Location, PlayerFlag, Runnable)} for actions performed by a player,</li>
 *     <li>{@link #checkWorldFlag(Chunk, WorldFlag, Runnable)} for natural/world-driven changes (explosions, mob griefing, growth...).</li>
 * </ul>
 */
public final class RegionProtectionListener implements Listener {
    public static final Map<UUID, Location> LAST_ENTITY_LOCATIONS = new ConcurrentHashMap<>();

    /**
     * Applies region/wilderness permission checks for an action performed by a player.
     *
     * <p>If the affected chunk is unclaimed and the world does not allow the flag, the action is
     * denied immediately. Otherwise, the decision is delegated to {@link RegionProtection#hasPermission}.
     *
     * @param player   the acting player (maybe {@code null}, in which case the action is allowed)
     * @param chunk    the chunk the action takes place in
     * @param location the precise location of the action
     * @param flag     the {@link PlayerFlag} flag required for the action
     * @param deny     callback executed when the action should be cancelled/denied
     */
    private void checkPlayerFlag(Player player, Chunk chunk, Location location, PlayerFlag flag, Runnable deny) {
        if (!ChunkManager.isChunkClaimed(chunk)
                && !WorldRules.isPlayerFlagAllowed(chunk.getWorld(), flag)) {
            deny.run();
            return;
        }
        RegionProtection.hasPermission(player, chunk, location, flag, null, deny);
    }

    /**
     * Applies region/wilderness flag checks for a non-player (natural or world-driven) change.
     *
     * <p>In a claimed chunk the owning region's world flag decides; in the wilderness the world's
     * {@link WorldRules} decide.
     *
     * @param chunk the chunk the change takes place in
     * @param flag  the {@link WorldFlag} flag that must be set to allow the change
     * @param deny  callback executed when the change should be cancelled/denied
     */
    private void checkWorldFlag(Chunk chunk, WorldFlag flag, Runnable deny) {
        if (ChunkManager.isChunkClaimed(chunk)) {
            Region region = ChunkManager.getRegionOwnsTheChunk(chunk);
            if (region != null && !region.isWorldFlagSet(flag)) {
                deny.run();
            }
        } else {
            if (!WorldRules.isWorldFlagAllowed(chunk.getWorld(), flag)) {
                deny.run();
            }
        }
    }

    /**
     * Called (from a per-tick task on non-Paper servers) to detect when an entity crosses a region
     * border and apply the {@link WorldFlag#ENTITY_GRIEFING} rules for Copper Golems.
     *
     * @param entity the entity whose movement is being evaluated
     */
    public static void onEntityMove(Entity entity) {
        try {
            Location from = LAST_ENTITY_LOCATIONS.get(entity.getUniqueId());
            Location to = entity.getLocation();

            if (from == null) {
                from = entity.getLocation();
            }

            Chunk fromChunk = from.getChunk();
            Chunk toChunk = to.getChunk();

            LAST_ENTITY_LOCATIONS.put(entity.getUniqueId(), to.clone());

            if (fromChunk.equals(toChunk)) {
                return;
            }

            if (entity instanceof CopperGolem golem) {
                Long spawnRegionId = CopperGolemTracker.getSpawnRegionId(golem);

                if (ChunkManager.isChunkClaimed(toChunk)) {
                    Region toRegion = ChunkManager.getRegionOwnsTheChunk(toChunk);

                    if (toRegion == null) {
                        return;
                    }

                    Long toRegionId = toRegion.getUniqueId();

                    if (spawnRegionId != null && spawnRegionId.equals(toRegionId)) {
                        return;
                    }

                    if (!toRegion.isWorldFlagSet(WorldFlag.ENTITY_GRIEFING)) {
                        entity.remove();
                    }
                } else {
                    if (spawnRegionId == null && !WorldRules.isWorldFlagAllowed(toChunk.getWorld(), WorldFlag.ENTITY_GRIEFING)) {
                        entity.remove();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof CopperGolem golem) {
            CopperGolemTracker.recordSpawnRegion(golem);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        LAST_ENTITY_LOCATIONS.remove(entity.getUniqueId());

        if (entity instanceof CopperGolem golem) {
            CopperGolemTracker.forgetGolem(golem);
        }
    }

    private static boolean canBeBrokenByProjectile(Block block) {
        return !block.isPreferredTool(new ItemStack(Material.AIR));
    }

    /**
     * Prevents placing blocks without the {@link PlayerFlag#PLACE_BLOCKS} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Runnable cancel = () -> event.setCancelled(true);
        checkPlayerFlag(event.getPlayer(), block.getChunk(), block.getLocation(),
                PlayerFlag.PLACE_BLOCKS, cancel);
    }

    /**
     * Prevents breaking blocks without the {@link PlayerFlag#BREAK_BLOCKS} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Runnable cancel = () -> event.setCancelled(true);
        checkPlayerFlag(event.getPlayer(), block.getChunk(), block.getLocation(),
                PlayerFlag.BREAK_BLOCKS, cancel);
    }

    /**
     * Prevents opening inventories of villagers and storage entities.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryInstanceOfEntityOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Entity entity)) {
            return;
        }

        Runnable cancel = () -> event.setCancelled(true);

        if (entity instanceof Villager villager) {
            Location location = villager.getLocation();
            checkPlayerFlag((Player) event.getPlayer(), location.getChunk(), location,
                    PlayerFlag.TRADE_VILLAGERS, cancel);
        } else if (entity instanceof ChestBoat
                || entity instanceof ChestedHorse
                || entity instanceof StorageMinecart
                || entity instanceof HopperMinecart) {
            Location location = entity.getLocation();
            checkPlayerFlag((Player) event.getPlayer(), location.getChunk(), location,
                    PlayerFlag.CONTAINERS, cancel);
        }
    }

    /**
     * Prevents emptying buckets without the {@link PlayerFlag#PLACE_BLOCKS} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Block relative = event.getBlockClicked().getRelative(event.getBlockFace());
        Location location = relative.getLocation();
        Runnable cancel = () -> event.setCancelled(true);
        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.PLACE_BLOCKS, cancel);
    }

    /**
     * Prevents filling buckets without the {@link PlayerFlag#BREAK_BLOCKS} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Block relative = event.getBlockClicked().getRelative(event.getBlockFace());
        Location location = relative.getLocation();
        Runnable cancel = () -> event.setCancelled(true);
        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.BREAK_BLOCKS, cancel);
    }

    /**
     * Prevents extinguishing fire without the {@link PlayerFlag#IGNITE} permission.
     */
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

    /**
     * Prevents trampling farmland / turtle eggs without the {@link PlayerFlag#BLOCK_TRAMPLING} permission.
     */
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

    /**
     * Prevents harvesting crops without the {@link PlayerFlag#HARVEST_CROPS} permission.
     */
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

    /**
     * Prevents spawning entities from spawn eggs without the {@link PlayerFlag#SPAWN_ENTITIES} permission.
     */
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

    /**
     * Handles most player interactions with blocks and placeable items in (claimed) chunks.
     * Uses Bukkit tags where available and centralizes the permission gating into {@link #checkPlayerFlag}.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block clicked = event.getClickedBlock();
        final Location location = (clicked != null ? clicked.getLocation() : player.getLocation());
        final Chunk chunk = location.getChunk();
        final Runnable cancel = () -> event.setCancelled(true);

        if (event.getItem() != null) {
            final Material itemType = event.getItem().getType();
            final String itn = itemType.name();

            final boolean placeSpawnItem =
                    itn.contains("BOAT") ||
                            itn.contains("ARMOR_STAND") ||
                            itn.contains("MINECART") ||
                            itn.contains("PAINTING") ||
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

    /**
     * Returns true for all shulker boxes using Bukkit tags with a simple fallback.
     */
    private boolean isShulkerBox(Material type) {
        if (Tag.SHULKER_BOXES.isTagged(type)) return true;
        final String n = type.name();
        return n.endsWith("SHULKER_BOX");
    }

    /**
     * Returns true for any kind of sign (standing, wall, hanging).
     */
    private boolean isAnySign(Material type) {
        if (Tag.SIGNS.isTagged(type)) return true;
        final String n = type.name();
        return n.endsWith("_HANGING_SIGN")
                || n.endsWith("_WALL_HANGING_SIGN")
                || n.endsWith("_SIGN")
                || n.endsWith("_WALL_SIGN");
    }

    /**
     * Returns true for blocks gated by the {@link PlayerFlag#CONTAINERS} flag.
     */
    private boolean isContainerLike(Material type) {
        if (type == Material.ENDER_CHEST) return false;
        if (Tag.CAMPFIRES.isTagged(type)) return true;
        if (isShulkerBox(type)) return true;

        switch (type) {
            case FURNACE:
            case SMOKER:
            case BLAST_FURNACE:
            case BREWING_STAND:
            case BARREL:
            case BEACON:
            case DROPPER:
            case DISPENSER:
            case CHISELED_BOOKSHELF:
            case CAULDRON:
            case LAVA_CAULDRON:
            case WATER_CAULDRON:
            case LODESTONE:
            case HOPPER:
                return true;
            default:
                final String n = type.name();

                if (n.contains("CHEST")) return true;
                return n.contains("SHELF");
        }
    }

    /**
     * Returns true for anvil variants.
     */
    private boolean isAnvil(Material type) {
        return type.name().contains("ANVIL");
    }

    /**
     * Returns true for archaeology brushing blocks.
     */
    private boolean isArchaeologyBlock(Material type) {
        return (type == Material.SUSPICIOUS_GRAVEL || type == Material.SUSPICIOUS_SAND);
    }

    /**
     * Returns true for archaeology brushing blocks when the player holds a brush.
     */
    private boolean isArchaeologyBlockWithBrush(Material type, Player player) {
        if (!isArchaeologyBlock(type)) return false;
        return player.getInventory().getItemInMainHand().getType() == Material.BRUSH;
    }

    /**
     * Returns true for small interactables handled under {@link PlayerFlag#GENERAL_INTERACTION}.
     */
    private boolean isSmallInteractable(Material type) {
        if (type == Material.CAKE) return true;
        if (type == Material.DECORATED_POT) return true;
        if (type == Material.FLOWER_POT) return true;
        final String n = type.name();
        return n.contains("POTTED");
    }

    /**
     * Returns true for blocks requiring {@link PlayerFlag#CONTAINERS} based on the item in hand.
     */
    private boolean isLecternOrVaultWithKey(Material type, Player player) {
        if (type == Material.LECTERN) {
            final Material inHand = player.getInventory().getItemInMainHand().getType();
            return inHand == Material.WRITTEN_BOOK || inHand == Material.WRITABLE_BOOK;
        }
        if (type == Material.VAULT) {
            return player.getInventory().getItemInMainHand().getType().name().contains("TRIAL_KEY");
        }
        return false;
    }

    /**
     * Returns true for blocks considered redstone interaction.
     */
    private boolean isRedstoneInteraction(Material type) {
        return switch (type) {
            case REPEATER, COMPARATOR, COMMAND_BLOCK, COMMAND_BLOCK_MINECART, REDSTONE, REDSTONE_WIRE, NOTE_BLOCK,
                 JUKEBOX, COMPOSTER, DAYLIGHT_DETECTOR -> true;
            default -> false;
        };
    }

    /**
     * Prevents brushing archaeology blocks without the {@link PlayerFlag#BREAK_BLOCKS} permission.
     */
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

    /**
     * Prevents punching item frames without the {@link PlayerFlag#BREAK_BLOCKS} permission.
     */
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

    /**
     * Prevents taking a book from a lectern without the {@link PlayerFlag#CONTAINERS} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent event) {
        Lectern lectern = event.getLectern();
        Location location = lectern.getLocation();
        Runnable cancel = () -> event.setCancelled(true);
        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.CONTAINERS, cancel);
    }

    /**
     * Prevents frost-walker trail creation without the {@link PlayerFlag#FROST_WALKER} permission.
     */
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

    /**
     * Prevents igniting blocks (player or natural) unless allowed by the relevant flag.
     */
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

    /**
     * Protects paintings and item frames from players, explosions and entity griefing.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingEntityBreak(HangingBreakByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity remover = event.getRemover();
        Location location = entity.getLocation();
        Chunk chunk = location.getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        if (entity instanceof Painting || entity instanceof ItemFrame) {
            if (remover instanceof Player player) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.BREAK_BLOCKS, cancel);
            } else if (Explosives.isExplosive(remover)) {
                checkWorldFlag(chunk, WorldFlag.EXPLOSION_DAMAGE, cancel);
            }
        } else {
            checkWorldFlag(chunk, WorldFlag.ENTITY_GRIEFING, cancel);
        }
    }

    /**
     * Prevents projectiles from breaking fragile blocks without the proper permission/world flag.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHitBreakableBlock(ProjectileHitEvent event) {
        Block hit = event.getHitBlock();
        if (hit == null || !canBeBrokenByProjectile(hit)) {
            return;
        }

        Projectile projectile = event.getEntity();
        ProjectileSource source = projectile.getShooter();
        Location location = hit.getLocation();
        Chunk chunk = location.getChunk();

        if (source instanceof Player player) {
            Runnable cancel = () -> event.setCancelled(true);
            checkPlayerFlag(player, chunk, location, PlayerFlag.BREAK_BLOCKS, cancel);
        } else if (projectile instanceof WitherSkull) {
            Runnable deny = () -> {
                event.getEntity().remove();
                event.setCancelled(true);
            };
            checkWorldFlag(chunk, WorldFlag.WITHER_DAMAGE, deny);
        } else {
            Runnable cancel = () -> event.setCancelled(true);
            checkWorldFlag(chunk, WorldFlag.PROJECTILES, cancel);
        }
    }

    /**
     * Routes entity damage to the appropriate player flag or natural world flag.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        Location location = entity.getLocation();
        Chunk chunk = location.getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        Player shooterPlayer = null;
        if (damager instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) {
                shooterPlayer = player;
            }
        }

        Player effectiveDamager = (damager instanceof Player player) ? player : shooterPlayer;

        if (effectiveDamager != null) {
            if (entity instanceof ArmorStand) {
                checkPlayerFlag(effectiveDamager, chunk, location, PlayerFlag.BREAK_BLOCKS, cancel);
            } else if (entity instanceof Player) {
                checkPlayerFlag(effectiveDamager, chunk, location, PlayerFlag.PVP, cancel);
            } else if (entity instanceof Monster || entity instanceof IronGolem) {
                checkPlayerFlag(effectiveDamager, chunk, location, PlayerFlag.DAMAGE_HOSTILE_ENTITIES, cancel);
            } else if (entity instanceof Mob) {
                checkPlayerFlag(effectiveDamager, chunk, location, PlayerFlag.DAMAGE_PASSIVE_ENTITIES, cancel);
            }
        } else if (damager instanceof Projectile) {
            checkWorldFlag(chunk, WorldFlag.PROJECTILES, cancel);
        } else if (Explosives.isExplosive(damager)) {
            checkWorldFlag(chunk, WorldFlag.EXPLOSION_DAMAGE, cancel);
        } else {
            checkWorldFlag(chunk, WorldFlag.ENTITY_DAMAGE, cancel);
        }
    }

    /**
     * Prevents shearing entities without the {@link PlayerFlag#INTERACT_ENTITIES} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Entity entity = event.getEntity();
        Location location = entity.getLocation();
        Runnable cancel = () -> event.setCancelled(true);
        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.INTERACT_ENTITIES, cancel);
    }

    /**
     * Prevents throwing ender pearls / splash potions without the relevant permission.
     */
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

    /**
     * Prevents eating chorus fruit (teleport) without the {@link PlayerFlag#TELEPORT} permission.
     */
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

    /**
     * Prevents splash / lingering potion effects without the {@link PlayerFlag#THROW_POTIONS} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        applyProjectileWorldOrPlayerFlag(event.getEntity(), PlayerFlag.THROW_POTIONS, () -> event.setCancelled(true));
    }

    /**
     * Prevents lingering potion clouds without the {@link PlayerFlag#THROW_POTIONS} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        applyProjectileWorldOrPlayerFlag(event.getEntity(), PlayerFlag.THROW_POTIONS, () -> event.setCancelled(true));
    }

    /**
     * Shared handling for potion projectiles: player-driven uses a flag, otherwise the world projectile flag.
     */
    private void applyProjectileWorldOrPlayerFlag(Projectile entity, PlayerFlag flag, Runnable cancel) {
        Location location = entity.getLocation();
        Chunk chunk = location.getChunk();

        if (entity.getShooter() instanceof Player player) {
            checkPlayerFlag(player, chunk, location, flag, cancel);
        } else {
            checkWorldFlag(chunk, WorldFlag.PROJECTILES, cancel);
        }
    }

    /**
     * Routes projectile/entity hits to the correct player flag or the world projectile flag.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHitEntity(ProjectileHitEvent event) {
        Entity entityHit = event.getHitEntity();
        if (entityHit == null || event.getEntity() instanceof ThrownPotion) {
            return;
        }

        Location location = entityHit.getLocation();
        Chunk chunk = location.getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        if (event.getEntity().getShooter() instanceof Player player) {
            if (entityHit instanceof Player) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.PVP, cancel);
            } else if (entityHit instanceof Monster || entityHit instanceof IronGolem) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.DAMAGE_HOSTILE_ENTITIES, cancel);
            } else if (entityHit instanceof Mob) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.DAMAGE_PASSIVE_ENTITIES, cancel);
            } else if (entityHit instanceof ArmorStand || entityHit instanceof ItemFrame || entityHit instanceof Painting) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.BREAK_BLOCKS, cancel);
            }
        } else {
            checkWorldFlag(chunk, WorldFlag.PROJECTILES, cancel);
        }
    }

    /**
     * Prevents dropping / picking up items without the {@link PlayerFlag#PICKUP_ITEMS} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Runnable cancel = () -> event.setCancelled(true);
        checkPlayerFlag(player, location.getChunk(), location,
                PlayerFlag.PICKUP_ITEMS, cancel);
    }

    /**
     * Prevents picking up items without the {@link PlayerFlag#PICKUP_ITEMS} permission.
     */
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

    /**
     * Prevents entering / damaging vehicles without the {@link PlayerFlag#VEHICLES} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player player) {
            Location location = event.getVehicle().getLocation();
            Runnable cancel = () -> event.setCancelled(true);
            checkPlayerFlag(player, location.getChunk(), location,
                    PlayerFlag.VEHICLES, cancel);
        }
    }

    /**
     * Prevents damaging vehicles without the {@link PlayerFlag#VEHICLES} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player player) {
            Location location = event.getVehicle().getLocation();
            Runnable cancel = () -> event.setCancelled(true);
            checkPlayerFlag(player, location.getChunk(), location,
                    PlayerFlag.VEHICLES, cancel);
        }
    }

    /**
     * Prevents leashing entities without the {@link PlayerFlag#INTERACT_ENTITIES} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeashEvent(PlayerLeashEntityEvent event) {
        Entity entity = event.getEntity();
        Location location = entity.getLocation();
        Runnable cancel = () -> event.setCancelled(true);
        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.INTERACT_ENTITIES, cancel);
    }

    /**
     * Prevents unleashing entities from fences without the {@link PlayerFlag#INTERACT_ENTITIES} permission.
     */
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

    /**
     * Prevents interacting with entities (villagers, armor stands, item frames, others).
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        applyEntityInteraction(event.getPlayer(), event.getRightClicked(), () -> event.setCancelled(true));
    }

    /**
     * Same as {@link #onPlayerInteractEntity} but for interactions at a specific point.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        applyEntityInteraction(event.getPlayer(), event.getRightClicked(), () -> event.setCancelled(true));
    }

    /**
     * Shared entity-interaction permission logic for villagers, armor stands, item frames and others.
     */
    private void applyEntityInteraction(Player player, Entity entity, Runnable cancel) {
        Location location = entity.getLocation();
        Chunk chunk = location.getChunk();

        if (entity instanceof Villager) {
            checkPlayerFlag(player, chunk, location, PlayerFlag.TRADE_VILLAGERS, cancel);
        } else if (entity instanceof ArmorStand) {
            checkPlayerFlag(player, chunk, location, PlayerFlag.ARMOR_STANDS, cancel);
        } else if (entity instanceof ItemFrame) {
            checkPlayerFlag(player, chunk, location, PlayerFlag.ITEM_FRAME_INTERACTION, cancel);
        } else if (!(entity instanceof Player)) {
            checkPlayerFlag(player, chunk, location, PlayerFlag.INTERACT_ENTITIES, cancel);
        }
    }

    /**
     * Prevents gliding with elytra without the {@link PlayerFlag#ELYTRA} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player player
                && event.isGliding() && isWearingElytra(player)) {
            Location location = player.getLocation();
            Runnable cancel = () -> event.setCancelled(true);
            checkPlayerFlag(player, location.getChunk(), location,
                    PlayerFlag.ELYTRA, cancel);
        }
    }

    /**
     * Prevents fall damage / flying into walls from dealing damage without the permission.
     */
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

    /**
     * Removes/limits explosions based on region/world {@link WorldFlag#EXPLOSION_DAMAGE} etc.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof WindCharge) {
            Chunk chunk = event.getLocation().getChunk();
            Runnable deny = () -> {
                entity.remove();
                event.setCancelled(true);
            };
            checkWorldFlag(chunk, WorldFlag.WINDCHARGE_BURST, deny);
        } else if (entity instanceof Wither || entity instanceof WitherSkull) {
            event.blockList().removeIf((block) -> {
                Chunk chunk = block.getChunk();

                if (ChunkManager.isChunkClaimed(chunk)) {
                    Region region = ChunkManager.getRegionOwnsTheChunk(chunk);
                    return region != null && !region.isWorldFlagSet(WorldFlag.WITHER_DAMAGE);
                } else {
                    return !WorldRules.isWorldFlagAllowed(chunk.getWorld(), WorldFlag.WITHER_DAMAGE);
                }
            });
        } else if (Explosives.isExplosive(entity)) {
            Chunk chunk = event.getLocation().getChunk();

            if (ChunkManager.isChunkClaimed(chunk)) {
                Region region = ChunkManager.getRegionOwnsTheChunk(chunk);

                if (region != null && !region.isWorldFlagSet(WorldFlag.EXPLOSION_DAMAGE)) {
                    event.setCancelled(true);
                }
            } else {
                boolean belowSeaOnly = Resources.<RegionsFile>get(ResourceType.Regions)
                        .getBoolean("special-feat.tnt-explodes-only-below-sea-level");

                List<Block> allowed = new ArrayList<>();

                for (Block block : event.blockList()) {
                    Chunk blockChunk = block.getChunk();

                    if (!ChunkManager.isChunkClaimed(blockChunk)) {
                        if (belowSeaOnly && entity instanceof TNTPrimed) {
                            if (block.getY() <= block.getWorld().getSeaLevel()) {
                                allowed.add(block);
                            }
                        } else {
                            allowed.add(block);
                        }
                    }
                }

                event.blockList().clear();
                event.blockList().addAll(allowed);
            }
        }
    }

    /**
     * Prevents block explosions in protected regions.
     */
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

    /**
     * Prevents fire spread, grass/mycelium growth, sculk spread and generic plant growth where disabled.
     */
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

    /**
     * Prevents crops/trees from growing without the {@link WorldFlag#PLANT_GROWTH} flag.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        Chunk chunk = event.getBlock().getLocation().getChunk();
        Runnable cancel = () -> event.setCancelled(true);
        checkWorldFlag(chunk, WorldFlag.PLANT_GROWTH, cancel);
    }

    /**
     * Prevents leaves from decaying without the {@link WorldFlag#LEAVES_DECAY} flag.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        Chunk chunk = event.getBlock().getLocation().getChunk();
        Runnable cancel = () -> event.setCancelled(true);
        checkWorldFlag(chunk, WorldFlag.LEAVES_DECAY, cancel);
    }

    /**
     * Prevents blocks from burning without the {@link WorldFlag#FIRE_SPREAD} flag.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Chunk chunk = event.getBlock().getLocation().getChunk();
        Runnable cancel = () -> event.setCancelled(true);
        checkWorldFlag(chunk, WorldFlag.FIRE_SPREAD, cancel);
    }

    /**
     * Prevents liquids from flowing between regions that disallow it.
     */
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

    /**
     * Prevents pistons from pushing blocks out of (or into) protected regions of another owner.
     */
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

    /**
     * Prevents sticky pistons from retracting blocks across protected region boundaries.
     */
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

    /**
     * Prevents dispensers from pushing items into another owner's region.
     */
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

    /**
     * Prevents non-player entity block changes (griefing) unless allowed.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        Block block = event.getBlock();
        Location location = block.getLocation();
        Chunk chunk = location.getChunk();

        if (entity instanceof Sheep || entity instanceof Goat || entity instanceof Cow || entity instanceof Villager
                || entity instanceof Bee || entity instanceof FallingBlock || block.getType().hasGravity()) {
            return;
        }

        Runnable cancel = () -> event.setCancelled(true);

        if (entity instanceof Player player) {
            checkPlayerFlag(player, chunk, location, PlayerFlag.BREAK_BLOCKS, cancel);
        } else if (entity instanceof Wither || entity instanceof WitherSkull) {
            checkWorldFlag(chunk, WorldFlag.WITHER_DAMAGE, cancel);
        } else {
            checkWorldFlag(chunk, WorldFlag.ENTITY_GRIEFING, cancel);
        }
    }

    /**
     * Prevents hostile/passive mob spawns where disabled by region/world flags.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Location location = event.getLocation();
        Chunk chunk = location.getChunk();
        Entity entity = event.getEntity();
        CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();

        boolean ignoreSpawners = Resources.<FlagsFile>get(ResourceType.Flags).doSpawnersIgnoreSpawnFlags();

        if (ignoreSpawners && spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        Runnable cancel = () -> event.setCancelled(true);

        if (entity instanceof Monster || entity instanceof IronGolem) {
            checkWorldFlag(chunk, WorldFlag.HOSTILE_ENTITY_SPAWN, cancel);
        } else if (entity instanceof Mob) {
            checkWorldFlag(chunk, WorldFlag.PASSIVE_ENTITY_SPAWN, cancel);
        }
    }

    /**
     * Prevents mobs from breaking doors unless allowed.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        Chunk chunk = event.getEntity().getLocation().getChunk();

        if (ChunkManager.isChunkClaimed(chunk)) {
            Region region = ChunkManager.getRegionOwnsTheChunk(chunk);

            if (!(event.getEntity() instanceof Player) && region != null
                    && !region.isWorldFlagSet(WorldFlag.ENTITY_GRIEFING)) {
                event.setCancelled(true);
            }
        } else {
            if (!WorldRules.isWorldFlagAllowed(chunk.getWorld(), WorldFlag.ENTITY_GRIEFING)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Prevents triggering raids without the {@link PlayerFlag#TRIGGER_RAID} permission.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRaidTrigger(RaidTriggerEvent event) {
        Player player = event.getPlayer();
        Raid raid = event.getRaid();
        Location location = raid.getLocation();
        Chunk chunk = location.getChunk();
        Runnable cancel = () -> {
            event.setCancelled(true);
            PotionEffect effect = player.getPotionEffect(PotionEffectType.RAID_OMEN);
            if (effect != null) {
                player.removePotionEffect(PotionEffectType.RAID_OMEN);
            }
        };

        if (!ChunkManager.isChunkClaimed(chunk)) {
            if (!WorldRules.isPlayerFlagAllowed(chunk.getWorld(), PlayerFlag.TRIGGER_RAID)) {
                cancel.run();
                return;
            }
        }

        checkPlayerFlag(player, chunk, location, PlayerFlag.TRIGGER_RAID, cancel);
    }

    /**
     * Prevents snow / ice from melting where disabled.
     */
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

    /**
     * Prevents minecarts from crossing into protected wilderness regions.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart)) {
            return;
        }

        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();

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

    /**
     * Prevents snowmen from leaving trails where disabled.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSnowGolemTrail(EntityBlockFormEvent event) {
        if (!(event.getEntity() instanceof Snowman)) {
            return;
        }

        Chunk chunk = event.getBlock().getChunk();
        Runnable cancel = () -> event.setCancelled(true);
        checkWorldFlag(chunk, WorldFlag.SNOWMAN_TRAILS, cancel);
    }

    /**
     * Prevents weather-driven snow from forming where disabled.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWeatherSnowForm(EntityBlockFormEvent event) {
        if (event.getNewState().getType() == Material.SNOW) {
            Chunk chunk = event.getBlock().getChunk();
            Runnable cancel = () -> event.setCancelled(true);
            checkWorldFlag(chunk, WorldFlag.WEATHER_SNOW, cancel);
        }
    }

    /**
     * Prevents trees from growing without the {@link WorldFlag#PLANT_GROWTH} flag.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTreeGrow(StructureGrowEvent event) {
        Chunk chunk = event.getLocation().getChunk();
        Runnable cancel = () -> event.setCancelled(true);
        checkWorldFlag(chunk, WorldFlag.PLANT_GROWTH, cancel);
    }

    private boolean isWearingElytra(Player player) {
        PlayerInventory inventory = player.getInventory();

        return inventory.getChestplate() != null && inventory.getChestplate().getType() == Material.ELYTRA;
    }

    private boolean isCropBlock(Block block) {
        Material type = block.getType();

        return type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES
                || type == Material.BEETROOTS || type == Material.PITCHER_PLANT || type == Material.NETHER_WART
                || type == Material.KELP || type == Material.CACTUS || type == Material.SEA_PICKLE
                || type == Material.RED_MUSHROOM || type == Material.BROWN_MUSHROOM || type == Material.SWEET_BERRIES
                || type == Material.SWEET_BERRY_BUSH;
    }
}

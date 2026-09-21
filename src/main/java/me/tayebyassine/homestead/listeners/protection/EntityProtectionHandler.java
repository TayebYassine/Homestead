package me.tayebyassine.homestead.listeners.protection;

import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.flags.WorldRules;
import me.tayebyassine.homestead.listeners.util.CopperGolemTracker;
import me.tayebyassine.homestead.listeners.util.Explosives;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.FlagsFile;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles region protection for entity-related events: damage, interactions,
 * griefing, spawning, deaths, explosions, projectiles, and creature spawns.
 */
public final class EntityProtectionHandler extends ProtectionHandler implements Listener {

    public static final Map<UUID, Location> LAST_ENTITY_LOCATIONS = new ConcurrentHashMap<>();

    /**
     * Called from a per-tick task on non-Paper servers to detect when an entity
     * crosses a region border and apply {@link WorldFlag#ENTITY_GRIEFING} rules
     * for Copper Golems.
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
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
            if (entity instanceof ArmorStand || isCushion(entity)) {
                checkPlayerFlag(effectiveDamager, chunk, location, PlayerFlag.BREAK_BLOCKS, cancel);
            } else if (entity instanceof Player victim) {
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
                        .isTntRestrictedToBelowSeaLevel();

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingEntityBreakByEntity(HangingBreakByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity remover = event.getRemover();
        Location location = entity.getLocation();
        Chunk chunk = location.getChunk();
        Runnable cancel = () -> event.setCancelled(true);

        if (isHangingDecor(entity)) {
            if (remover instanceof Player player) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.BREAK_BLOCKS, cancel);
            } else if (Explosives.isExplosive(remover)) {
                checkWorldFlag(chunk, WorldFlag.EXPLOSION_DAMAGE, cancel);
            }
        } else {
            checkWorldFlag(chunk, WorldFlag.ENTITY_GRIEFING, cancel);
        }
    }

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
            } else if (entityHit instanceof ArmorStand
                    || entityHit instanceof ItemFrame
                    || entityHit instanceof Painting
                    || isCushion(entityHit)) {
                checkPlayerFlag(player, chunk, location, PlayerFlag.BREAK_BLOCKS, cancel);
            }
        } else {
            checkWorldFlag(chunk, WorldFlag.PROJECTILES, cancel);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggProjectileHit(ProjectileHitEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Egg egg)) {
            return;
        }

        ProjectileSource source = egg.getShooter();
        if (!(source instanceof Player player)) {
            return;
        }

        Location location;
        Block hitBlock = event.getHitBlock();
        Entity hitEntity = event.getHitEntity();

        if (hitBlock != null) {
            location = hitBlock.getLocation();
        } else if (hitEntity != null) {
            location = hitEntity.getLocation();
        } else {
            location = egg.getLocation();
        }

        Chunk chunk = location.getChunk();
        Runnable cancel = () -> {
            egg.remove();
            event.setCancelled(true);
        };

        checkPlayerFlag(player, chunk, location, PlayerFlag.THROW_EGGS, cancel);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        applyProjectileWorldOrPlayerFlag(event.getEntity(), PlayerFlag.THROW_POTIONS, () -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        applyProjectileWorldOrPlayerFlag(event.getEntity(), PlayerFlag.THROW_POTIONS, () -> event.setCancelled(true));
    }

    private void applyProjectileWorldOrPlayerFlag(Projectile entity, PlayerFlag flag, Runnable cancel) {
        Location location = entity.getLocation();
        Chunk chunk = location.getChunk();

        if (entity.getShooter() instanceof Player player) {
            checkPlayerFlag(player, chunk, location, flag, cancel);
        } else {
            checkWorldFlag(chunk, WorldFlag.PROJECTILES, cancel);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Entity entity = event.getEntity();
        Location location = entity.getLocation();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.INTERACT_ENTITIES, cancel);
    }

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        applyEntityInteraction(event.getPlayer(), event.getRightClicked(), () -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        applyEntityInteraction(event.getPlayer(), event.getRightClicked(), () -> event.setCancelled(true));
    }

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
     * Shared entity-interaction permission logic for villagers, armor stands,
     * item frames, and other entities.
     */
    void applyEntityInteraction(Player player, Entity entity, Runnable cancel) {
        Location location = entity.getLocation();
        Chunk chunk = location.getChunk();

        if (entity instanceof Villager) {
            checkPlayerFlag(player, chunk, location, PlayerFlag.TRADE_VILLAGERS, cancel);
        } else if (entity instanceof ArmorStand) {
            checkPlayerFlag(player, chunk, location, PlayerFlag.ARMOR_STANDS, cancel);
        } else if (entity instanceof ItemFrame) {
            checkPlayerFlag(player, chunk, location, PlayerFlag.ITEM_FRAME_INTERACTION, cancel);
        } else if (entity instanceof Vehicle || isCushion(entity)) {
            checkPlayerFlag(player, chunk, location, PlayerFlag.VEHICLES, cancel);
        } else if (!(entity instanceof Player)) {
            checkPlayerFlag(player, chunk, location, PlayerFlag.INTERACT_ENTITIES, cancel);
        }
    }
}

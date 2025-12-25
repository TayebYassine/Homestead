package tfagaming.projects.minecraft.homestead.listeners;

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
import org.bukkit.event.Cancellable;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableSubArea;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.*;

public final class RegionProtectionListener implements Listener {
	public static final Map<UUID, Location> lastLocations = new HashMap<>();

	// Blocks protection

	/**
	 * Static function to listen when an entity move.
	 */
	public static void onEntityMove(Entity entity) {
		Location from = lastLocations.get(entity.getUniqueId());
		Location to = entity.getLocation();

		if (from == null) {
			from = entity.getLocation();
		}

		Chunk fromChunk = from.getChunk();
		Chunk toChunk = to.getChunk();

		lastLocations.put(entity.getUniqueId(), to.clone());

		if (fromChunk.equals(toChunk)) {
			return;
		}

		if (ChunksManager.isChunkClaimed(toChunk)) {
			if (entity instanceof CopperGolem) {
				Region fromRegion = ChunksManager.getRegionOwnsTheChunk(fromChunk);
				Region toRegion = ChunksManager.getRegionOwnsTheChunk(toChunk);

				if (toRegion == null) {
					return;
				}

				if (fromRegion == null) {
					if (!toRegion.isWorldFlagSet(WorldFlags.WILDERNESS_COPPER_GOLEMS)) {
						entity.remove();
					}
				} else if (!fromRegion.getUniqueId().equals(toRegion.getUniqueId())) {
					if (!toRegion.isWorldFlagSet(WorldFlags.WILDERNESS_COPPER_GOLEMS)) {
						entity.remove();
					}
				}
			}
		}
	}

	// Block place
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location location = block.getLocation();
		Chunk chunk = block.getChunk();

		RegionProtection.hasPermission(player, chunk, location, PlayerFlags.PLACE_BLOCKS, null, () -> {
			event.setCancelled(true);
		});
	}

	// Block break
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location location = block.getLocation();
		Chunk chunk = location.getChunk();

		RegionProtection.hasPermission(player, chunk, location, PlayerFlags.BREAK_BLOCKS, null, () -> {
			event.setCancelled(true);
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryInstanceOfEntityOpen(InventoryOpenEvent event) {
		Inventory inventory = event.getInventory();
		InventoryHolder holder = inventory.getHolder();

		if (holder instanceof Entity entity) {
			if (entity instanceof Villager villager) {
				Player player = (Player) event.getPlayer();
				Location location = villager.getLocation();
				Chunk chunk = location.getChunk();

				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.TRADE_VILLAGERS, null, () -> {
					event.setCancelled(true);
				});
			} else if (entity instanceof ChestBoat
					|| entity instanceof ChestedHorse
					|| entity instanceof StorageMinecart
					|| entity instanceof HopperMinecart) {
				Player player = (Player) event.getPlayer();
				Location location = entity.getLocation();
				Chunk chunk = location.getChunk();

				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.CONTAINERS, null, () -> {
					event.setCancelled(true);
				});
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		Block blockClicked = event.getBlockClicked();
		BlockFace blockFace = event.getBlockFace();
		Block blockRelative = blockClicked.getRelative(blockFace);
		Location location = blockRelative.getLocation();
		Chunk chunk = location.getChunk();

		RegionProtection.hasPermission(player, chunk, location, PlayerFlags.PLACE_BLOCKS, null, () -> {
			event.setCancelled(true);
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();
		Block blockClicked = event.getBlockClicked();
		BlockFace blockFace = event.getBlockFace();
		Block blockRelative = blockClicked.getRelative(blockFace);
		Location location = blockRelative.getLocation();
		Chunk chunk = location.getChunk();

		RegionProtection.hasPermission(player, chunk, location, PlayerFlags.BREAK_BLOCKS, null, () -> {
			event.setCancelled(true);
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerExtinguishFire(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		Block targetBlock = player.getTargetBlock(null, 5);

		if (action != Action.LEFT_CLICK_BLOCK) {
			return;
		}

		if (targetBlock.getType() == Material.FIRE) {
			Location location = targetBlock.getLocation();
			Chunk chunk = location.getChunk();

			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.IGNITE, null, () -> {
				event.setCancelled(true);
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerTrampleBlock(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block blockClicked = event.getClickedBlock();
		Action action = event.getAction();

		if (action != Action.PHYSICAL || blockClicked == null) {
			return;
		}

		if (List.of(Material.FARMLAND, Material.TURTLE_EGG).contains(blockClicked.getType())) {
			Location location = blockClicked.getLocation();
			Chunk chunk = location.getChunk();

			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.BLOCK_TRAMPLING, null, () -> {
				event.setCancelled(true);
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerHarvestCrop(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block blockClicked = event.getClickedBlock();

		if (blockClicked == null) {
			return;
		}

		if (isCropBlock(blockClicked)) {
			Location location = blockClicked.getLocation();
			Chunk chunk = location.getChunk();

			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.HARVEST_CROPS, null, () -> {
				event.setCancelled(true);
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPlaceSpawnEgg(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		Block blockClicked = event.getClickedBlock();
		ItemStack item = event.getItem();

		if (item == null || blockClicked == null || (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR)) {
			return;
		}

		if (item.getType().name().endsWith("_SPAWN_EGG")) {
			Location location = blockClicked.getLocation();
			Chunk chunk = location.getChunk();

			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.SPAWN_ENTITIES, null, () -> {
				event.setCancelled(true);
			});
		}
	}

	/**
	 * Handles most player interaction with blocks and certain placeable items in claimed chunks.
	 * Uses Bukkit tags where available and centralizes permission gating to reduce branching and duplication.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final Block clicked = event.getClickedBlock();
		final Chunk chunk = (clicked != null ? clicked.getLocation().getChunk() : player.getLocation().getChunk());

		if (!ChunksManager.isChunkClaimed(chunk)) return;
		if (PlayerUtils.isOperator(player)) return;

		final Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
		if (region == null) return;

		final boolean isOwner = player.getUniqueId().equals(region.getOwnerId());
		if (isOwner) return;

		final SerializableSubArea subArea = (clicked != null) ? region.findSubAreaHasLocationInside(clicked.getLocation())
				: region.findSubAreaHasLocationInside(player.getLocation());

		if (event.getItem() != null) {
			final Material itemType = event.getItem().getType();
			final String itn = itemType.name();

			final boolean placeSpawnItem =
					itn.contains("BOAT") ||
							itn.contains("ARMOR_STAND") ||
							itn.contains("MINECART") ||
							itn.contains("PAINTING") ||
							itemType == Material.BONE_MEAL;

			if (placeSpawnItem) {
				if (!requireFlag(region, subArea, player, PlayerFlags.PLACE_BLOCKS, event)) return;
			}
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clicked != null) {
			final Material type = clicked.getType();

			if (isShulkerBox(type)) {
				if (!requireFlag(region, subArea, player, PlayerFlags.CONTAINERS, event)) return;
			}

			if (isAnySign(type)) {
				if (!requireFlag(region, subArea, player, PlayerFlags.GENERAL_INTERACTION, event)) return;
			}

			if (isContainerLike(type)) {
				if (!requireFlag(region, subArea, player, PlayerFlags.CONTAINERS, event)) return;
				return;
			}

			if (isAnvil(type)) {
				if (!requireFlag(region, subArea, player, PlayerFlags.USE_ANVIL, event)) return;
				return;
			}

			if (Tag.TRAPDOORS.isTagged(type)) {
				if (!requireFlag(region, subArea, player, PlayerFlags.TRAP_DOORS, event)) return;
				return;
			}

			if (Tag.DOORS.isTagged(type) || type.name().contains("DOOR")) {
				if (!requireFlag(region, subArea, player, PlayerFlags.DOORS, event)) return;
				return;
			}

			if (isArchaeologyBlockWithBrush(type, player)) {
				if (!requireFlag(region, subArea, player, PlayerFlags.BREAK_BLOCKS, event)) return;
				return;
			}

			if (Tag.BUTTONS.isTagged(type)) {
				if (!requireFlag(region, subArea, player, PlayerFlags.BUTTONS, event)) return;
				return;
			}

			if (type.name().contains("FENCE_GATE")) {
				if (!requireFlag(region, subArea, player, PlayerFlags.FENCE_GATES, event)) return;
				return;
			}

			if (isSmallInteractable(type)) {
				if (!requireFlag(region, subArea, player, PlayerFlags.GENERAL_INTERACTION, event)) return;
				return;
			}

			if (isLecternOrVaultWithKey(type, player)) {
				if (!requireFlag(region, subArea, player, PlayerFlags.CONTAINERS, event)) return;
				return;
			}

			if (type.name().endsWith("_BED")) {
				if (!requireFlag(region, subArea, player, PlayerFlags.SLEEP, event)) return;
				return;
			}

			if (type == Material.LEVER) {
				if (!requireFlag(region, subArea, player, PlayerFlags.LEVERS, event)) return;
				return;
			}

			if (type == Material.BELL) {
				if (!requireFlag(region, subArea, player, PlayerFlags.USE_BELLS, event)) return;
				return;
			}

			if (isRedstoneInteraction(type)) {
				if (!requireFlag(region, subArea, player, PlayerFlags.REDSTONE, event)) return;
			}
			return;
		}

		if (event.getAction() == Action.PHYSICAL && clicked != null) {
			final Material type = clicked.getType();

			if (Tag.PRESSURE_PLATES.isTagged(type)) {
				if (!requireFlag(region, subArea, player, PlayerFlags.PRESSURE_PLATES, event)) return;
				return;
			}

			if (type == Material.TRIPWIRE) {
				if (!requireFlag(region, subArea, player, PlayerFlags.TRIGGER_TRIPWIRE, event)) {
				}
			}
		}
	}

	/**
	 * Centralized permission gate. Cancels the event if the player does not have the given flag.
	 */
	private boolean requireFlag(Region region, SerializableSubArea subArea, Player player, long flag, Cancellable event) {
		final boolean allowed = (subArea != null)
				? PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player, flag, true)
				: PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, flag, true);

		if (!allowed) {
			event.setCancelled(true);
			return false;
		}
		return true;
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
	 * Returns true for blocks gated by the CONTAINERS flag in your logic.
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
	 * Returns true if the block is an anvil variant.
	 */
	private boolean isAnvil(Material type) {
		return type.name().contains("ANVIL");
	}

	/**
	 * Returns true for archaeology brushing blocks when the player holds a brush.
	 */
	private boolean isArchaeologyBlockWithBrush(Material type, Player player) {
		if (!(type == Material.SUSPICIOUS_GRAVEL || type == Material.SUSPICIOUS_SAND)) return false;
		return player.getInventory().getItemInMainHand().getType() == Material.BRUSH;
	}

	/**
	 * Returns true for small interactables handled under GENERAL_INTERACTION.
	 */
	private boolean isSmallInteractable(Material type) {
		if (type == Material.CAKE) return true;
		if (type == Material.DECORATED_POT) return true;
		if (type == Material.FLOWER_POT) return true;
		final String n = type.name();
		return n.contains("POTTED");
	}

	/**
	 * Returns true if the block requires CONTAINERS based on item-in-hand logic (lectern with book, vault with trial key).
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
	 * Returns true for blocks considered redstone interaction in your logic.
	 */
	private boolean isRedstoneInteraction(Material type) {
		return switch (type) {
			case REPEATER, COMPARATOR, COMMAND_BLOCK, COMMAND_BLOCK_MINECART, REDSTONE, REDSTONE_WIRE, NOTE_BLOCK,
				 JUKEBOX, COMPOSTER, DAYLIGHT_DETECTOR -> true;
			default -> false;
		};
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPunchFrame(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof ItemFrame) { // GlowItemFrame extends ItemFrame
			if (event.getDamager() instanceof Player player) {
				Location location = entity.getLocation();
				Chunk chunk = location.getChunk();

				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.BREAK_BLOCKS, null, () -> {
					event.setCancelled(true);
				});
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent event) {
		Player player = event.getPlayer();
		Lectern lectern = event.getLectern();
		Location location = lectern.getLocation();
		Chunk chunk = location.getChunk();

		RegionProtection.hasPermission(player, chunk, location, PlayerFlags.CONTAINERS, null, () -> {
			event.setCancelled(true);
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityBlockForm(EntityBlockFormEvent event) {
		Entity entity = event.getEntity();
		Block block = event.getBlock();

		if (entity instanceof Player player) {
			Location location = block.getLocation();
			Chunk chunk = location.getChunk();

			EntityEquipment equipment = player.getEquipment();

			if (equipment == null) {
				return;
			}

			ItemStack boots = equipment.getBoots();

			if (boots != null && boots.getEnchantments().containsKey(Enchantment.FROST_WALKER)) {
				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.FROST_WALKER, null, () -> {
					event.setCancelled(true);
				});
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location location = block.getLocation();
		Chunk chunk = location.getChunk();

		if (player == null && ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (region != null && !region.isWorldFlagSet(WorldFlags.FIRE_SPREAD)) {
				event.setCancelled(true);
			}

			return;
		}

		RegionProtection.hasPermission(player, chunk, location, PlayerFlags.IGNITE, null, () -> {
			event.setCancelled(true);
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onHangingEntityBreak(HangingBreakByEntityEvent event) {
		Entity entity = event.getEntity();
		Entity remover = event.getRemover();

		if (entity instanceof Painting || entity instanceof ItemFrame) {
			if (remover instanceof Player player) {
				Location location = entity.getLocation();
				Chunk chunk = location.getChunk();

				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.BREAK_BLOCKS, null, () -> {
					event.setCancelled(true);
				});
			} else if (Explosives.isExplosive(remover)) {
				Location location = entity.getLocation();
				Chunk chunk = location.getChunk();

				if (ChunksManager.isChunkClaimed(chunk)) {
					Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

					if (region != null && !region.isWorldFlagSet(WorldFlags.EXPLOSIONS_DAMAGE)) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	// Entities protection
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHitBreakableBlock(ProjectileHitEvent event) {
		Block hit = event.getHitBlock();

		if (hit == null) {
			return;
		}

		Projectile projectile = event.getEntity();
		ProjectileSource source = projectile.getShooter();

		if (!canBeBrokenByProjectile(hit)) {
			return;
		}

		Location location = hit.getLocation();
		Chunk chunk = location.getChunk();

		if (source instanceof Player player) {
			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.BREAK_BLOCKS, null, () -> {
				event.setCancelled(true);
			});
		} else if (projectile instanceof WitherSkull) {
			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.WITHER_DAMAGE)) {
					event.getEntity().remove();
					event.setCancelled(true);
				}
			}
		} else if (ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (region != null && !region.isWorldFlagSet(WorldFlags.WILDERNESS_DISPENSERS)) {
				event.setCancelled(true);
			}
		}
	}

	private static boolean canBeBrokenByProjectile(Block block) {
		return !block.isPreferredTool(new ItemStack(Material.AIR));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		Entity damager = event.getDamager();
		Location location = entity.getLocation();
		Chunk chunk = location.getChunk();

		if (damager instanceof Player player) {
			if (entity instanceof ArmorStand) {
				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.BREAK_BLOCKS, null, () -> {
					event.setCancelled(true);
				});
			} else if (entity instanceof Player) {
				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.PVP, null, () -> {
					event.setCancelled(true);
				});
			} else if (entity instanceof Monster || entity instanceof IronGolem) {
				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.DAMAGE_HOSTILE_ENTITIES, null, () -> {
					event.setCancelled(true);
				});
			} else if (entity instanceof Mob) {
				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.DAMAGE_PASSIVE_ENTITIES, null, () -> {
					event.setCancelled(true);
				});
			}
		} else if (Explosives.isExplosive(entity)) {
			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.EXPLOSIONS_DAMAGE)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerShearEntity(PlayerShearEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getEntity();
		Location location = entity.getLocation();
		Chunk chunk = location.getChunk();

		RegionProtection.hasPermission(player, chunk, location, PlayerFlags.INTERACT_ENTITIES, null, () -> {
			event.setCancelled(true);
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteractThrowThrowable(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		if (item == null) {
			return;
		}

		Material type = item.getType();
		long flag = -1L;

		if (type == Material.ENDER_PEARL) {
			flag = PlayerFlags.TELEPORT;
		} else if (type == Material.SPLASH_POTION || type == Material.LINGERING_POTION) {
			flag = PlayerFlags.THROW_POTIONS;
		} else {
			return;
		}

		Location launchLoc = player.getLocation();
		Chunk chunk = launchLoc.getChunk();

		RegionProtection.hasPermission(player, chunk, launchLoc, flag, null, () -> {
			event.setCancelled(true);
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerEatChorusFruit(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		Location location = player.getLocation();
		Chunk chunk = location.getChunk();
		ItemStack item = event.getItem();

		if (item.getType().equals(Material.CHORUS_FRUIT)) {
			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.TELEPORT, null, () -> {
				event.setCancelled(true);
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		Projectile entity = event.getEntity();
		ProjectileSource shooter = entity.getShooter();
		Location location = entity.getLocation();
		Chunk chunk = location.getChunk();

		if (shooter instanceof Player player) {
			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.THROW_POTIONS, null, () -> {
				event.setCancelled(true);
			});
		} else if (ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (region != null && !region.isWorldFlagSet(WorldFlags.WILDERNESS_DISPENSERS)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
		Projectile entity = event.getEntity();
		ProjectileSource shooter = entity.getShooter();
		Location location = entity.getLocation();
		Chunk chunk = location.getChunk();

		if (shooter instanceof Player player) {
			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.THROW_POTIONS, null, () -> {
				event.setCancelled(true);
			});
		} else if (ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (region != null && !region.isWorldFlagSet(WorldFlags.WILDERNESS_DISPENSERS)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHitEntity(ProjectileHitEvent event) {
		Projectile entity = event.getEntity();
		ProjectileSource shooter = entity.getShooter();

		Entity entityHit = event.getHitEntity();

		if (shooter instanceof Player player && entity instanceof ThrownPotion) {
			Location location = entity.getLocation();
			Chunk chunk = location.getChunk();

			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.THROW_POTIONS, null, () -> {
				event.setCancelled(true);
			});
		} else if (shooter instanceof Player player && entityHit instanceof LivingEntity) {
			Location location = entityHit.getLocation();
			Chunk chunk = location.getChunk();

			if (entityHit instanceof Player) {
				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.PVP, null, () -> {
						event.setCancelled(true);
				});
			} else if (entityHit instanceof Monster || entityHit instanceof IronGolem) {
				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.DAMAGE_HOSTILE_ENTITIES, null, () -> {
					event.setCancelled(true);
				});
			} else if (entityHit instanceof Mob) {
				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.DAMAGE_PASSIVE_ENTITIES, null, () -> {
					event.setCancelled(true);
				});
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (shouldCancelItemTransfer(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPickupItem(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (shouldCancelItemTransfer(player)) {
			event.setCancelled(true);
		}
	}

	private boolean shouldCancelItemTransfer(Player player) {
		if (player == null) return false;
		if (PlayerUtils.isOperator(player)) return false;

		Chunk chunk = player.getLocation().getChunk();
		if (!ChunksManager.isChunkClaimed(chunk)) return false;

		Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
		if (region == null) return false;
		if (player.getUniqueId().equals(region.getOwnerId())) return false;

		SerializableSubArea subArea = region.findSubAreaHasLocationInside(player.getLocation());

		boolean allowed = (subArea != null)
				? PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player, PlayerFlags.PICKUP_ITEMS, true)
				: PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PICKUP_ITEMS, true);

		return !allowed;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onVehicleEnter(VehicleEnterEvent event) {
		Vehicle vehicle = event.getVehicle();
		Location location = vehicle.getLocation();
		Chunk chunk = location.getChunk();
		Entity entity = event.getEntered();

		if (entity instanceof Player player) {
			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.VEHICLES, null, () -> {
				event.setCancelled(true);
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onVehicleDamage(VehicleDamageEvent event) {
		Vehicle vehicle = event.getVehicle();
		Location location = vehicle.getLocation();
		Chunk chunk = location.getChunk();
		Entity entity = event.getAttacker();

		if (entity instanceof Player player) {
			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.BREAK_BLOCKS, null, () -> {
				event.setCancelled(true);
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getEntity();
		Location location = entity.getLocation();
		Chunk chunk = location.getChunk();

		RegionProtection.hasPermission(player, chunk, location, PlayerFlags.INTERACT_ENTITIES, null, () -> {
			event.setCancelled(true);
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onLeashEvent(PlayerLeashEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getEntity();
		Location location = entity.getLocation();
		Block block = location.getBlock();
		Chunk chunk = location.getChunk();

		if (block instanceof Fence) {
			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.INTERACT_ENTITIES, null, () -> {
				event.setCancelled(true);
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getEntity();
		Location location = entity.getLocation();
		Block block = location.getBlock();
		Chunk chunk = location.getChunk();

		if (block instanceof Fence) {
			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.INTERACT_ENTITIES, null, () -> {
				event.setCancelled(true);
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		if (shouldCancelEntityInteraction(player, entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		if (shouldCancelEntityInteraction(player, entity)) {
			event.setCancelled(true);
		}
	}

	/**
	 * Returns true if the interaction should be cancelled based on region ownership,
	 * sub-area membership and the required flag for the clicked entity type.
	 * <p>
	 * Required flags:
	 * <ul>
	 *   <li>ArmorStand → {@code PlayerFlags.ARMOR_STANDS}</li>
	 *   <li>Item Frames → {@code PlayerFlags.ITEM_FRAME_ROTATION}</li>
	 *   <li>Villager trading → {@code PlayerFlags.TRADE_VILLAGERS}</li>
	 *   <li>All other entities → {@code PlayerFlags.INTERACT_ENTITIES}</li>
	 * </ul>
	 *
	 * @param player the interacting player
	 * @param entity the clicked entity
	 * @return true if the event should be cancelled
	 */
	private boolean shouldCancelEntityInteraction(Player player, Entity entity) {
		if (player == null || entity == null) return false;
		if (PlayerUtils.isOperator(player)) return false;

		Chunk chunk = entity.getLocation().getChunk();
		if (!ChunksManager.isChunkClaimed(chunk)) return false;

		Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
		if (region == null) return false;

		if (player.getUniqueId().equals(region.getOwnerId())) return false;

		long requiredFlag = -1L;
		EntityType type = entity.getType();

		if (type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME) {
			requiredFlag = PlayerFlags.ITEM_FRAME_ROTATION;
		} else if (entity instanceof Villager) {
			requiredFlag = PlayerFlags.TRADE_VILLAGERS;
		} else if (entity instanceof ArmorStand) {
			requiredFlag = PlayerFlags.ARMOR_STANDS;
		} else if (!(entity instanceof Player)) {
			requiredFlag = PlayerFlags.INTERACT_ENTITIES;
		}

		if (requiredFlag != -1L) {
			return true;
		}

		SerializableSubArea subArea = region.findSubAreaHasLocationInside(entity.getLocation());
		boolean allowed = (subArea != null)
				? PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player, requiredFlag, true)
				: PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, requiredFlag, true);

		return !allowed;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityToggleGlide(EntityToggleGlideEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof Player player) {
			Location location = player.getLocation();
			Chunk chunk = location.getChunk();

			if (event.isGliding() && isWearingElytra(player)) {
				RegionProtection.hasPermission(player, chunk, location, PlayerFlags.ELYTRA, null, () -> {
					event.setCancelled(true);
				});
			}
		}
	}


	// World protection

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerFallDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		EntityDamageEvent.DamageCause cause = event.getCause();

		if (cause != DamageCause.FALL && cause != DamageCause.FLY_INTO_WALL) return;

		if (entity instanceof Player player) {
			Location location = player.getLocation();
			Chunk chunk = location.getChunk();

			RegionProtection.hasPermission(player, chunk, location, PlayerFlags.TAKE_FALL_DAMAGE, null, () -> {
				event.setCancelled(true);
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof WindCharge) {
			Chunk chunk = event.getLocation().getChunk();

			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.WINDCHARGE_BURST)) {
					entity.remove();
					event.setCancelled(true);
				}
			}
		} else if (entity instanceof Wither || entity instanceof WitherSkull) {
			boolean updated = event.blockList().removeIf((block) -> {
				Chunk chunk = block.getChunk();

				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				return region != null && !region.isWorldFlagSet(WorldFlags.WITHER_DAMAGE);
			});

			if (updated) {
				event.setCancelled(true);
			}
		} else if (Explosives.isExplosive(entity)) {
			Chunk chunk = event.getLocation().getChunk();

			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.EXPLOSIONS_DAMAGE)) {
					event.setCancelled(true);
				}
			} else {
				List<Block> allowedBlocks = new ArrayList<Block>();

				for (Block block : event.blockList()) {
					Location blockLocation = block.getLocation();
					Chunk blockChunk = blockLocation.getChunk();

					if (!ChunksManager.isChunkClaimed(blockChunk)) {
						allowedBlocks.add(block);
					}
				}

				event.blockList().clear();
				event.blockList().addAll(allowedBlocks);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent event) {
		Block block = event.getBlock();

		if (block.getType().equals(Material.AIR)) {
			Location location = block.getLocation();
			Chunk chunk = location.getChunk();

			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.EXPLOSIONS_DAMAGE)) {
					event.setCancelled(true);
				}
			} else {
				List<Block> allowedBlocks = new ArrayList<Block>();

				for (Block b : event.blockList()) {
					Location blockLocation = b.getLocation();
					Chunk blockChunk = blockLocation.getChunk();

					if (!ChunksManager.isChunkClaimed(blockChunk)) {
						allowedBlocks.add(b);
					}
				}

				event.blockList().clear();
				event.blockList().addAll(allowedBlocks);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent event) {
		BlockState newState = event.getNewState();
		Block block = event.getBlock();
		Block source = event.getSource();

		Location location = block.getLocation();
		Chunk chunk = location.getChunk();

		if (newState.getType() == Material.FIRE) {
			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.FIRE_SPREAD)) {
					event.setCancelled(true);
				}
			}
		} else if (source.getType() == Material.GRASS_BLOCK || source.getType() == Material.MYCELIUM) {
			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.GRASS_GROWTH)) {
					event.setCancelled(true);
				}
			}
		} else if (event.getSource().getType() == Material.SCULK_CATALYST) {
			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.SCULK_SPREAD)) {
					event.setCancelled(true);
				}
			}
		} else {
			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.PLANT_GROWTH)) {
					event.setCancelled(true);
				}
			}
		}
	}

	public void onBlockGrow(BlockGrowEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();
		Chunk chunk = location.getChunk();

		if (ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (region != null && !region.isWorldFlagSet(WorldFlags.PLANT_GROWTH)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onLeavesDecay(LeavesDecayEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();
		Chunk chunk = location.getChunk();

		if (ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (region != null && !region.isWorldFlagSet(WorldFlags.LEAVES_DECAY)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();
		Chunk chunk = location.getChunk();

		if (ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (region != null && !region.isWorldFlagSet(WorldFlags.FIRE_SPREAD)) {
				event.setCancelled(true);
			}
		}
	}

	// TODO fix this
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onLiquidFlow(BlockFromToEvent event) {
		Chunk fromChunk = event.getBlock().getChunk();
		Chunk toChunk = event.getToBlock().getChunk();

		if (!fromChunk.equals(toChunk)) {
			if (ChunksManager.isChunkClaimed(toChunk) && ChunksManager.isChunkClaimed(fromChunk)) {
				event.setCancelled(false);
			} else if (ChunksManager.isChunkClaimed(toChunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(toChunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.LIQUID_FLOW)) {
					event.setCancelled(true);
				}
			}
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

		if (!this.canPistonMoveBlock(affectedBlocks, direction, piston.getLocation().getChunk(), false)) {
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

		if (!this.canPistonMoveBlock(affectedBlocks, direction, piston.getLocation().getChunk(), true)) {
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

				if (!chunk.equals(pistonChunk) && ChunksManager.isChunkClaimed(chunk)) {
					Region pistonChunkRegion = ChunksManager.getRegionOwnsTheChunk(pistonChunk);
					UUID pistonChunkOwner = pistonChunkRegion == null ? null : pistonChunkRegion.getOwnerId();
					UUID targetChunkOwner = Objects.requireNonNull(ChunksManager.getRegionOwnsTheChunk(chunk)).getOwnerId();

					if (pistonChunkRegion != null && pistonChunkOwner != null && pistonChunkOwner.equals(targetChunkOwner)) {
						return true;
					}

					Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

					if (region != null && !region.isWorldFlagSet(WorldFlags.WILDERNESS_PISTONS)) {
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

				if (!chunk.equals(pistonChunk) && ChunksManager.isChunkClaimed(chunk)) {
					Region pistonChunkRegion = ChunksManager.getRegionOwnsTheChunk(pistonChunk);
					UUID pistonChunkOwner = pistonChunkRegion == null ? null : pistonChunkRegion.getOwnerId();
					UUID targetChunkOwner = Objects.requireNonNull(ChunksManager.getRegionOwnsTheChunk(chunk)).getOwnerId();

					if (pistonChunkRegion != null && pistonChunkOwner != null && pistonChunkOwner.equals(targetChunkOwner)) {
						return true;
					}

					Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

					if (region != null && !region.isWorldFlagSet(WorldFlags.WILDERNESS_PISTONS)) {
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
			if (ChunksManager.isChunkClaimed(targetChunk)) {
				Region dispenserChunkRegion = ChunksManager.getRegionOwnsTheChunk(block.getLocation().getChunk());
				UUID dispenserChunkOwner = dispenserChunkRegion == null ? null : dispenserChunkRegion.getOwnerId();
				UUID targetChunkOwner = Objects.requireNonNull(ChunksManager.getRegionOwnsTheChunk(targetChunk)).getOwnerId();

				if (dispenserChunkRegion != null && dispenserChunkOwner != null && dispenserChunkOwner.equals(targetChunkOwner)) {
					return;
				}

				Region region = ChunksManager.getRegionOwnsTheChunk(targetChunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.WILDERNESS_DISPENSERS)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		Entity entity = event.getEntity();
		Block block = event.getBlock();
		Location location = block.getLocation();
		Chunk chunk = location.getChunk();

		if (entity instanceof Sheep || entity instanceof Goat || entity instanceof Cow || entity instanceof Villager || entity instanceof Bee) {
			return;
		}

		if (entity instanceof Wither || entity instanceof WitherSkull) {
			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.WITHER_DAMAGE)) {
					event.setCancelled(true);
				}
			}
		} else if (!(entity instanceof Player)) {
			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.ENTITIES_GRIEF)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		Location location = event.getLocation();
		Chunk chunk = location.getChunk();
		Entity entity = event.getEntity();
		CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();

		boolean ignoreSpawners = Homestead.config.get("flags-configuration.spawners");

		if (ignoreSpawners && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
			return;
		}

		if (ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (entity instanceof Monster || entity instanceof IronGolem) {
				if (region != null && !region.isWorldFlagSet(WorldFlags.HOSTILE_ENTITIES_SPAWN)) {
					event.setCancelled(true);
				}
			} else if (entity instanceof Mob) {
				if (region != null && !region.isWorldFlagSet(WorldFlags.PASSIVE_ENTITIES_SPAWN)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageByEntity2(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		Entity damager = event.getDamager();
		Chunk chunk = entity.getLocation().getChunk();

		if (ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (!(damager instanceof Player)) {
				if (region != null && !region.isWorldFlagSet(WorldFlags.ENTITIES_DAMAGE_ENTITIES)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityBreakDoor(EntityBreakDoorEvent event) {
		Entity entity = event.getEntity();
		Chunk chunk = entity.getLocation().getChunk();

		if (ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (!(entity instanceof Player)) {
				if (region != null && !region.isWorldFlagSet(WorldFlags.ENTITIES_GRIEF)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onRaidTrigger(RaidTriggerEvent event) {
		Player player = event.getPlayer();
		Raid raid = event.getRaid();
		Location location = raid.getLocation();
		Chunk chunk = location.getChunk();

		RegionProtection.hasPermission(player, chunk, location, PlayerFlags.TRIGGER_RAID, null, () -> {
			event.setCancelled(true);

			PotionEffect effect = event.getPlayer().getPotionEffect(PotionEffectType.RAID_OMEN);

			if (effect != null) {
				event.getPlayer().removePotionEffect(PotionEffectType.RAID_OMEN);
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockFade(BlockFadeEvent event) {
		Material blockType = event.getBlock().getType();
		Chunk chunk = event.getBlock().getLocation().getChunk();

		if (ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (blockType == Material.SNOW) {
				if (region != null && !region.isWorldFlagSet(WorldFlags.SNOW_MELTING)) {
					event.setCancelled(true);
				}
			} else if (blockType == Material.ICE) {
				if (region != null && !region.isWorldFlagSet(WorldFlags.ICE_MELTING)) {
					event.setCancelled(true);
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onVehicleMove(VehicleMoveEvent event) {
		if (!(event.getVehicle() instanceof Minecart)) {
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();

		Chunk fromChunk = from.getChunk();
		Chunk toChunk = to.getChunk();

		if (fromChunk.equals(toChunk)) {
			return;
		}

		if (ChunksManager.isChunkClaimed(toChunk)) {
			Region fromRegion = ChunksManager.getRegionOwnsTheChunk(fromChunk);
			Region toRegion = ChunksManager.getRegionOwnsTheChunk(toChunk);

			if (fromRegion == null) {
				if (toRegion != null && !toRegion.isWorldFlagSet(WorldFlags.WILDERNESS_MINECARTS)) {
					event.getVehicle().remove();
				}
			} else if (toRegion != null && !fromRegion.getUniqueId().equals(toRegion.getUniqueId())) {
				if (!toRegion.isWorldFlagSet(WorldFlags.WILDERNESS_MINECARTS)) {
					event.getVehicle().remove();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onWitherBlockChange(EntityChangeBlockEvent event) {
		Entity entity = event.getEntity();

		if (entity.getType() == EntityType.WITHER || entity.getType() == EntityType.WITHER_SKULL) {
			Block block = event.getBlock();
			Chunk chunk = block.getChunk();

			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.WITHER_DAMAGE)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSnowGolemTrail(EntityBlockFormEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof Snowman) {
			Block block = event.getBlock();
			Chunk chunk = block.getChunk();

			if (ChunksManager.isChunkClaimed(chunk)) {
				Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (region != null && !region.isWorldFlagSet(WorldFlags.SNOWMAN_TRAILS)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTreeGrow(StructureGrowEvent event) {
		Chunk chunk = event.getLocation().getChunk();

		if (ChunksManager.isChunkClaimed(chunk)) {
			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

			if (region != null && !region.isWorldFlagSet(WorldFlags.PLANT_GROWTH)) {
				event.setCancelled(true);
			}
		}
	}

	// Helper functions

	private boolean isWearingElytra(Player player) {
		return player.getInventory().getChestplate() != null &&
				player.getInventory().getChestplate().getType() == Material.ELYTRA;
	}

	private boolean isCropBlock(Block block) {
		Material type = block.getType();

		return type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES
				|| type == Material.BEETROOTS || type == Material.PITCHER_PLANT || type == Material.NETHER_WART
				|| type == Material.KELP || type == Material.CACTUS || type == Material.SEA_PICKLE
				|| type == Material.RED_MUSHROOM || type == Material.BROWN_MUSHROOM || type == Material.SWEET_BERRIES
				|| type == Material.SWEET_BERRY_BUSH;
	}

	public static class Explosives {
		public static boolean isExplosive(Entity e) {
			if (e == null) return false;

			if (e instanceof TNTPrimed) return true;
			if (e instanceof Creeper) return true;
			if (e instanceof Fireball) return true;
			if (e instanceof WitherSkull) return true;
			if (e instanceof EnderCrystal) return true;
			if (e instanceof WindCharge) return true;

			if (e instanceof Player) {
				return false;
			}

			return switch (e.getType()) {
				case TNT, TNT_MINECART, CREEPER, FIREBALL, WITHER_SKULL, END_CRYSTAL, WIND_CHARGE -> true;
				default -> false;
			};
		}
	}

	public static class Projectiles {
		public static boolean isProjectile(Entity e) {
			return e instanceof Projectile;
		}

		public static boolean isPlayerProjectile(Entity e) {
			return e instanceof Projectile p &&
					p.getShooter() instanceof Player;
		}

		public static boolean isMobProjectile(Entity e) {
			return e instanceof Projectile p &&
					p.getShooter() instanceof LivingEntity &&
					!(p.getShooter() instanceof Player);
		}
	}

	public static class RegionProtection {
		public static boolean hasPermission(Player player,
											Chunk chunk,
											Location location,
											long flag) {
			if (player != null && PlayerUtils.isOperator(player)) return true;

			if (!ChunksManager.isChunkClaimed(chunk)) return true;

			Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
			if (region == null) return true;

			assert player != null;
			if (player.getUniqueId().equals(region.getOwnerId())) return true;

			SerializableSubArea subArea = region.findSubAreaHasLocationInside(location);

			return subArea != null
					? PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player, flag, true)
					: PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, flag, true);
		}

		public static void hasPermission(Player player,
										 Chunk chunk,
										 Location location,
										 long flag,
										 Runnable onTrue,
										 Runnable onFalse) {
			boolean allowed = hasPermission(player, chunk, location, flag);

			if (allowed && onTrue != null) onTrue.run();
			if (!allowed && onFalse != null) onFalse.run();
		}
	}
}

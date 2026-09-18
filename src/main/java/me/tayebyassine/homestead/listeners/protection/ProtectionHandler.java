package me.tayebyassine.homestead.listeners.protection;

import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.listeners.util.RegionProtection;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.flags.WorldRules;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Fence;

/**
 * Base class for region protection handlers providing shared permission
 * checking and material classification helpers.
 */
public abstract class ProtectionHandler {

    protected void checkPlayerFlag(Player player, Chunk chunk, Location location, PlayerFlag flag, Runnable deny) {
        if (!ChunkManager.isChunkClaimed(chunk)
                && !WorldRules.isPlayerFlagAllowed(chunk.getWorld(), flag)) {
            deny.run();
            return;
        }

        RegionProtection.hasPermission(player, chunk, location, flag, null, deny);
    }

    protected void checkWorldFlag(Chunk chunk, WorldFlag flag, Runnable deny) {
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

    protected boolean isShulkerBox(Material type) {
        if (Tag.SHULKER_BOXES.isTagged(type)) return true;

        return type.name().endsWith("SHULKER_BOX");
    }

    protected boolean isAnySign(Material type) {
        if (Tag.SIGNS.isTagged(type)) return true;

        return type.name().endsWith("_HANGING_SIGN")
                || type.name().endsWith("_WALL_HANGING_SIGN")
                || type.name().endsWith("_SIGN")
                || type.name().endsWith("_WALL_SIGN");
    }

    protected boolean isContainerLike(Material type) {
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

    protected boolean isAnvil(Material type) {
        return type.name().contains("ANVIL");
    }

    protected boolean isArchaeologyBlock(Material type) {
        return type == Material.SUSPICIOUS_GRAVEL || type == Material.SUSPICIOUS_SAND;
    }

    protected boolean isArchaeologyBlockWithBrush(Material type, Player player) {
        if (!isArchaeologyBlock(type)) return false;

        return player.getInventory().getItemInMainHand().getType() == Material.BRUSH;
    }

    protected boolean isSmallInteractable(Material type) {
        if (type == Material.CAKE) return true;
        if (type == Material.DECORATED_POT) return true;
        if (type == Material.FLOWER_POT) return true;

        return type.name().contains("POTTED");
    }

    protected boolean isLecternOrVaultWithKey(Material type, Player player) {
        if (type == Material.LECTERN) {
            Material inHand = player.getInventory().getItemInMainHand().getType();
            return inHand == Material.WRITTEN_BOOK || inHand == Material.WRITABLE_BOOK;
        }

        if (type == Material.VAULT) {
            return player.getInventory().getItemInMainHand().getType().name().contains("TRIAL_KEY");
        }

        return false;
    }

    protected boolean isRedstoneInteraction(Material type) {
        return switch (type) {
            case REPEATER, COMPARATOR, COMMAND_BLOCK, COMMAND_BLOCK_MINECART, REDSTONE, REDSTONE_WIRE, NOTE_BLOCK,
                 JUKEBOX, COMPOSTER, DAYLIGHT_DETECTOR -> true;
            default -> false;
        };
    }

    protected boolean isCropBlock(Block block) {
        Material type = block.getType();

        return type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES
                || type == Material.BEETROOTS || type == Material.PITCHER_PLANT || type == Material.NETHER_WART
                || type == Material.KELP || type == Material.CACTUS || type == Material.SEA_PICKLE
                || type == Material.RED_MUSHROOM || type == Material.BROWN_MUSHROOM || type == Material.SWEET_BERRIES
                || type == Material.SWEET_BERRY_BUSH;
    }

    protected boolean isWearingElytra(Player player) {
        PlayerInventory inventory = player.getInventory();
        return inventory.getChestplate() != null && inventory.getChestplate().getType() == Material.ELYTRA;
    }

    protected boolean canBeBrokenByProjectile(Block block) {
        return !block.isPreferredTool(new ItemStack(Material.AIR));
    }

    protected boolean isHangingDecor(Entity entity) {
        return entity instanceof Painting
                || entity instanceof ItemFrame
                || entity instanceof ArmorStand
                || isCushion(entity);
    }

    protected boolean isCushion(Entity entity) {
        return isCushion(entity.getType());
    }

    protected boolean isCushion(EntityType type) {
        return isCushion(type.name());
    }

    protected boolean isCushion(String name) {
        return name.contains("CUSHION");
    }
}

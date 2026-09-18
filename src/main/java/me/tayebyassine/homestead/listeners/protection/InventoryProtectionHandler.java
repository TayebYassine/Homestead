package me.tayebyassine.homestead.listeners.protection;

import me.tayebyassine.homestead.flags.PlayerFlag;
import org.bukkit.Location;
import org.bukkit.block.Lectern;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Handles region protection for inventory/container access: villager trading,
 * chest boats, storage minecarts, and lectern book access.
 */
public final class InventoryProtectionHandler extends ProtectionHandler implements Listener {

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent event) {
        Lectern lectern = event.getLectern();
        Location location = lectern.getLocation();
        Runnable cancel = () -> event.setCancelled(true);

        checkPlayerFlag(event.getPlayer(), location.getChunk(), location,
                PlayerFlag.CONTAINERS, cancel);
    }
}

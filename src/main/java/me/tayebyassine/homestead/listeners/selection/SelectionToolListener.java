package me.tayebyassine.homestead.listeners.selection;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.borders.particles.SelectedAreaParticlesSpawner;
import me.tayebyassine.homestead.models.serialize.SeBlock;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.minecraft.platform.PlatformBridge;
import me.tayebyassine.homestead.util.minecraft.threads.TaskHandle;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the region selection tool: left/right-clicking sets the first/second corner of a
 * selection, shows a particle outline, and cancels the session when the tool is swapped out.
 */
public final class SelectionToolListener implements Listener {

    private static final Set<UUID> COOLDOWN = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Selection> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, TaskHandle> TASKS = new ConcurrentHashMap<>();

    /**
     * Cancels the active selection session for the given player and stops the particle outline.
     *
     * @param player the player whose session should be cancelled
     */
    public static void cancelPlayerSession(Player player) {
        if (SESSIONS.containsKey(player.getUniqueId())) {
            SESSIONS.remove(player.getUniqueId());
            SelectedAreaParticlesSpawner.cancelTask(player);
        }
        cancelTask(player);
    }

    public static void cancelTask(TaskHandle task, Player player) {
        if (task != null) {
            TASKS.remove(player.getUniqueId());
            task.cancel();
        }
    }

    public static void cancelTask(Player player) {
        TaskHandle task = TASKS.get(player.getUniqueId());
        if (task != null) {
            TASKS.remove(player.getUniqueId());
            task.cancel();
        }
    }

    /**
     * Returns the player's selection if both corners have been set, otherwise {@code null}.
     *
     * @param player the player to query
     * @return a completed {@link Selection} or {@code null}
     */
    public static Selection getPlayerSession(Player player) {
        Selection selection = SESSIONS.get(player.getUniqueId());
        if (selection == null || selection.getSecondPosition() == null || selection.getFirstPosition() == null) {
            return null;
        }
        return selection;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() == getSelectionToolType()) {
            UUID playerId = player.getUniqueId();
            SESSIONS.putIfAbsent(playerId, new Selection());
            Selection selection = SESSIONS.get(playerId);

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);

                if (COOLDOWN.contains(player.getUniqueId())) return;

                Block firstPosition = event.getClickedBlock();

                if (selection.getSecondPosition() != null && !sameWorld(selection.getSecondPosition(), firstPosition)) {
                    return;
                }

                selection.setFirstPosition(firstPosition);
                COOLDOWN.add(player.getUniqueId());
                Homestead.getInstance().runAsyncTaskLater(() -> COOLDOWN.remove(player.getUniqueId()), 1);

                sendActionBarMessage(player, "firstCorner");

                if (selection.getSecondPosition() != null && selection.getFirstPosition() != null) {
                    sendActionBarMessage(player, "selectionDone");
                    new SelectedAreaParticlesSpawner(player, new SeBlock(selection.getFirstPosition()),
                            new SeBlock(selection.getSecondPosition()));
                }

            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);

                if (COOLDOWN.contains(player.getUniqueId())) return;

                Block secondPosition = event.getClickedBlock();

                if (selection.getFirstPosition() != null && !sameWorld(selection.getFirstPosition(), secondPosition)) {
                    return;
                }

                selection.setSecondPosition(secondPosition);
                COOLDOWN.add(player.getUniqueId());
                Homestead.getInstance().runAsyncTaskLater(() -> COOLDOWN.remove(player.getUniqueId()), 1);

                sendActionBarMessage(player, "secondCorner");

                if (selection.getSecondPosition() != null && selection.getFirstPosition() != null) {
                    sendActionBarMessage(player, "selectionDone");
                    new SelectedAreaParticlesSpawner(player, new SeBlock(selection.getFirstPosition()),
                            new SeBlock(selection.getSecondPosition()));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        UUID playerId = player.getUniqueId();

        if (newItem == null || newItem.getType() != getSelectionToolType()) {
            cancelPlayerSession(player);
        } else {
            SESSIONS.putIfAbsent(playerId, new Selection());
            sendActionBarMessage(player, "none");
        }
    }

    private void sendActionBarMessage(Player player, String path) {
        cancelTask(player);

        TaskHandle task = Homestead.getInstance().runSyncTimerTask(
                () -> PlatformBridge.get().sendActionBar(player, Resources.<RegionsFile>get(ResourceType.Regions).getString("selection-tool.messages." + path)), 20);

        TASKS.put(player.getUniqueId(), task);
    }

    private Material getSelectionToolType() {
        String itemString = Resources.<RegionsFile>get(ResourceType.Regions).getString("selection-tool.item");
        return Material.getMaterial(itemString);
    }

    private boolean sameWorld(Block loc1, Block loc2) {
        if (loc1.getWorld() == null || loc2.getWorld() == null) return false;
        return loc1.getWorld().getUID().equals(loc2.getWorld().getUID());
    }

    /**
     * Holds the two corners of a region selection.
     */
    public static class Selection {
        private Block firstPosition;
        private Block secondPosition;

        public Block getFirstPosition() {
            return firstPosition;
        }

        public void setFirstPosition(Block firstPosition) {
            this.firstPosition = firstPosition;
        }

        public Block getSecondPosition() {
            return secondPosition;
        }

        public void setSecondPosition(Block secondPosition) {
            this.secondPosition = secondPosition;
        }
    }
}
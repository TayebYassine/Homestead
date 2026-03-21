package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.borders.SelectedAreaParticlesSpawner;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBlock;
import tfagaming.projects.minecraft.homestead.tools.minecraft.papermc.TaskHandle;
import tfagaming.projects.minecraft.homestead.tools.minecraft.platform.PlatformBridge;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SelectionToolListener implements Listener {

	private static final Set<UUID> cooldowns = ConcurrentHashMap.newKeySet();
	private static final Map<UUID, Selection> sessions = new ConcurrentHashMap<>();
	private static final Map<UUID, TaskHandle> tasks = new ConcurrentHashMap<>();

	public static void cancelPlayerSession(Player player) {
		if (sessions.containsKey(player.getUniqueId())) {
			sessions.remove(player.getUniqueId());
			SelectedAreaParticlesSpawner.cancelTask(player);
		}
		cancelTask(player);
	}

	public static void cancelTask(TaskHandle task, Player player) {
		if (task != null) {
			tasks.remove(player.getUniqueId());
			task.cancel();
		}
	}

	public static void cancelTask(Player player) {
		TaskHandle task = tasks.get(player.getUniqueId());
		if (task != null) {
			tasks.remove(player.getUniqueId());
			task.cancel();
		}
	}

	public static Selection getPlayerSession(Player player) {
		Selection selection = sessions.get(player.getUniqueId());
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
			sessions.putIfAbsent(playerId, new Selection());
			Selection selection = sessions.get(playerId);

			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				event.setCancelled(true);

				if (cooldowns.contains(player.getUniqueId())) return;

				Block firstPosition = event.getClickedBlock();

				if (selection.getSecondPosition() != null && !sameWorld(selection.getSecondPosition(), firstPosition)) {
					return;
				}

				selection.setFirstPosition(firstPosition);
				cooldowns.add(player.getUniqueId());
				Homestead.getInstance().runAsyncTaskLater(() -> cooldowns.remove(player.getUniqueId()), 1);

				sendActionBarMessage(player, "firstCorner");

				if (selection.getSecondPosition() != null && selection.getFirstPosition() != null) {
					sendActionBarMessage(player, "selectionDone");
					new SelectedAreaParticlesSpawner(player, new SerializableBlock(selection.getFirstPosition()),
							new SerializableBlock(selection.getSecondPosition()));
				}

			} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				event.setCancelled(true);

				if (cooldowns.contains(player.getUniqueId())) return;

				Block secondPosition = event.getClickedBlock();

				if (selection.getFirstPosition() != null && !sameWorld(selection.getFirstPosition(), secondPosition)) {
					return;
				}

				selection.setSecondPosition(secondPosition);
				cooldowns.add(player.getUniqueId());
				Homestead.getInstance().runAsyncTaskLater(() -> cooldowns.remove(player.getUniqueId()), 1);

				sendActionBarMessage(player, "secondCorner");

				if (selection.getSecondPosition() != null && selection.getFirstPosition() != null) {
					sendActionBarMessage(player, "selectionDone");
					new SelectedAreaParticlesSpawner(player, new SerializableBlock(selection.getFirstPosition()),
							new SerializableBlock(selection.getSecondPosition()));
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
			sessions.putIfAbsent(playerId, new Selection());
			sendActionBarMessage(player, "none");
		}
	}

	private void sendActionBarMessage(Player player, String path) {
		cancelTask(player);

		TaskHandle task = Homestead.getInstance().runSyncTimerTask(
				() -> PlatformBridge.get().sendActionBar(player, Homestead.config.getString("selection-tool.messages." + path)), 20);

		tasks.put(player.getUniqueId(), task);
	}

	private Material getSelectionToolType() {
		String itemString = Homestead.config.getString("selection-tool.item");
		return Material.getMaterial(itemString);
	}

	private boolean sameWorld(Block loc1, Block loc2) {
		if (loc1.getWorld() == null || loc2.getWorld() == null) return false;
		return loc1.getWorld().getUID().equals(loc2.getWorld().getUID());
	}

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
package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.MenusFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.PersistentChunkTicket;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility.ButtonData;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.DelayedTeleport;

import java.util.ArrayList;
import java.util.List;

public final class RegionClaimedChunks {
	private List<SerializableChunk> chunks;

	public RegionClaimedChunks(Player player, Region region) {
		this.chunks = region.getChunks();

		PaginationMenu gui = new PaginationMenu(
				MenuUtility.getTitle(11), 9 * 5,
				MenuUtility.getNextPageButton(),
				MenuUtility.getPreviousPageButton(),
				getItems(player, region),
				(_player, event) -> new RegionMenu(player, region),
				(_player, context) -> {
					if (context.getIndex() >= chunks.size()) return;

					if (RegionManager.findRegion(region.getUniqueId()) == null) {
						player.closeInventory();
						return;
					}

					SerializableChunk chunk = chunks.get(context.getIndex());

					if (context.getEvent().isRightClick()) {
						if (!player.hasPermission("homestead.region.teleport")) {
							Messages.send(player, 212);
							return;
						}

						player.closeInventory();

						new DelayedTeleport(player, chunk.bukkitLocation());
					} else if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
						if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
							Messages.send(player, 30);
							return;
						}

						int totalForcedLoadedChunks = region.getChunks().stream().filter(SerializableChunk::isForceLoaded).toList().size();
						int maxForceLoadedChunks = Limits.getRegionLimit(region, Limits.LimitType.MAX_FORCE_LOADED_CHUNKS);

						if (totalForcedLoadedChunks >= maxForceLoadedChunks && !chunk.isForceLoaded()) {
							Messages.send(player, 116);
							return;
						}

						Chunk bukkitChunk = chunk.bukkit();
						final boolean newState = !chunk.isForceLoaded();

						region.setChunkForceLoaded(chunk, newState);

						if (newState) {
							PersistentChunkTicket.addPersistent(Homestead.getInstance(), bukkitChunk);
						} else {
							PersistentChunkTicket.removePersistent(Homestead.getInstance(), bukkitChunk);
						}

						PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

						chunks = region.getChunks();
						context.getInstance().setItems(getItems(player, region));
					} else {
						if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_CHUNK_UNCLAIM)) {
							Cooldown.sendCooldownMessage(player);
							return;
						}

						if (!ChunkManager.isChunkClaimed(chunk.bukkit()) || !ChunkManager.isChunkClaimedByRegion(region, chunk.bukkit())) {
							return;
						}

						if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
								RegionControlFlags.UNCLAIM_CHUNKS)) {
							return;
						}

						Cooldown.startCooldown(player, Cooldown.Type.REGION_CHUNK_UNCLAIM);

						int before = region.getChunks().size();
						ChunkManager.unclaimChunk(region.getUniqueId(), chunk.bukkit());

						if (region.getChunks().size() < before) {
							double chunkPrice = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("chunk-price");
							if (chunkPrice > 0) PlayerBank.deposit(region.getOwner(), chunkPrice);
						}

						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

						ChunkBorder.show(player);

						chunks = region.getChunks();
						context.getInstance().setItems(getItems(player, region));
					}
				});

		gui.addActionButton(1, MenuUtility.getButton(73, new Placeholder()
				.add("{max-chunks}", Limits.getRegionLimit(region, Limits.LimitType.CHUNKS_PER_REGION))
		), null);

		gui.open(player, MenuUtility.getEmptySlot());
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < chunks.size(); i++) {
			SerializableChunk chunk = chunks.get(i);

			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{index}", i + 1)
					.add("{chunk-claimedat}", Formatter.getDate(chunk.getClaimedAt()))
					.add("{chunk-location}", Formatter.getLocation(chunk.bukkitLocation()))
					.add("{chunk-is-loaded}", Formatter.getBoolean(chunk.isForceLoaded()));

			ButtonData data = MenuUtility.getButtonData(33);

			if (data.getOriginalType().equals("CUSTOM::GETBYWORLD")) {
				data.setOriginalType(switch (chunk.bukkitLocation().getWorld().getEnvironment()) {
					case NETHER -> Resources.<MenusFile>get(ResourceType.Menus).get("button-types.world.nether");
					case THE_END -> Resources.<MenusFile>get(ResourceType.Menus).get("button-types.world.the_end");
					default -> Resources.<MenusFile>get(ResourceType.Menus).get("button-types.world.overworld");
				});
			}

			items.add(MenuUtility.getButton(data, placeholder));
		}

		return items;
	}
}
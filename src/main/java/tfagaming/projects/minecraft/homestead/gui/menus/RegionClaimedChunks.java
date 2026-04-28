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
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionChunk;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.MenusFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;

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
import java.util.Objects;

public final class RegionClaimedChunks {
	private List<RegionChunk> chunks;

	public RegionClaimedChunks(Player player, Region region) {
		this.chunks = ChunkManager.getChunksOfRegion(region);

		PaginationMenu.builder(11, 9 * 5)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(getItems(player, region))
				.fillEmptySlots()
				.goBack((_player, event) -> new RegionMenu(player, region))
				.onClick((_player, context) -> handleChunkClick(player, region, context))
				.actionButton(1, MenuUtility.getButton(73, new Placeholder()
						.add("{max-chunks}", Limits.getRegionLimit(region, Limits.LimitType.CHUNKS_PER_REGION))), null)
				.build()
				.open(player);
	}

	private void handleChunkClick(Player player, Region region, PaginationMenu.ClickContext context) {
		if (context.getIndex() >= chunks.size()) return;

		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		RegionChunk chunk = chunks.get(context.getIndex());

		if (context.getEvent().isRightClick()) {
			handleTeleport(player, chunk);
		} else if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
			handleToggleForceLoad(player, region, chunk, context);
		} else {
			handleUnclaim(player, region, chunk, context);
		}
	}

	private void handleTeleport(Player player, RegionChunk chunk) {
		if (!player.hasPermission("homestead.region.teleport")) {
			Messages.send(player, 212);
			return;
		}

		player.closeInventory();

		Homestead.getInstance().runLocationTask(chunk.toBukkitDisplayLocation(), () -> {
			new DelayedTeleport(player, chunk.toBukkitLocation());
		});
	}

	private void handleToggleForceLoad(Player player, Region region, RegionChunk chunk, PaginationMenu.ClickContext context) {
		if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, 30);
			return;
		}

		int totalForcedLoadedChunks = ChunkManager.getChunksOfRegion(region).stream()
				.filter(RegionChunk::isForceLoaded).toList().size();
		int maxForceLoadedChunks = Limits.getRegionLimit(region, Limits.LimitType.MAX_FORCE_LOADED_CHUNKS);

		if (totalForcedLoadedChunks >= maxForceLoadedChunks && !chunk.isForceLoaded()) {
			Messages.send(player, 116);
			return;
		}

		Chunk bukkitChunk = chunk.toBukkit();
		assert bukkitChunk != null;

		boolean newState = !chunk.isForceLoaded();
		chunk.setForceLoaded(newState);

		if (newState) {
			PersistentChunkTicket.addPersistent(Homestead.getInstance(), bukkitChunk);
		} else {
			PersistentChunkTicket.removePersistent(Homestead.getInstance(), bukkitChunk);
		}

		PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

		chunks = ChunkManager.getChunksOfRegion(region);
		context.getInstance().setItems(getItems(player, region));
	}

	private void handleUnclaim(Player player, Region region, RegionChunk chunk, PaginationMenu.ClickContext context) {
		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_CHUNK_UNCLAIM)) {
			Cooldown.sendCooldownMessage(player);
			return;
		}

		if (!ChunkManager.isChunkClaimed(chunk.toBukkit()) || !ChunkManager.isChunkClaimedByRegion(region, chunk.toBukkit())) {
			return;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player, RegionControlFlags.UNCLAIM_CHUNKS)) {
			return;
		}

		Cooldown.startCooldown(player, Cooldown.Type.REGION_CHUNK_UNCLAIM);

		int before = ChunkManager.getChunksOfRegion(region).size();
		ChunkManager.unclaimChunk(region.getUniqueId(), chunk.toBukkit());

		if (ChunkManager.getChunksOfRegion(region).size() < before) {
			double chunkPrice = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("chunk-price");
			if (chunkPrice > 0) PlayerBank.deposit(region.getOwner(), chunkPrice);
		}

		PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
		ChunkBorder.show(player);

		chunks = ChunkManager.getChunksOfRegion(region);
		context.getInstance().setItems(getItems(player, region));
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < chunks.size(); i++) {
			RegionChunk chunk = chunks.get(i);

			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{index}", i + 1)
					.add("{chunk-claimedat}", Formatter.getDate(chunk.getClaimedAt()))
					.add("{chunk-location}", Formatter.getLocation(Objects.requireNonNull(chunk.toBukkitDisplayLocation())))
					.add("{chunk-is-loaded}", Formatter.getBoolean(chunk.isForceLoaded()));

			ButtonData data = MenuUtility.getButtonData(33);

			if (data.getOriginalType().equals("CUSTOM::GETBYWORLD")) {
				data.setOriginalType(switch (chunk.getWorld().getEnvironment()) {
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
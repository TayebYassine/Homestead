package me.tayebyassine.homestead.gui.menus.region;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.chunk.ChunkUnclaimEvent;
import me.tayebyassine.homestead.borders.ChunkBorder;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.gui.helpers.ButtonData;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionChunk;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.MenusFile;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.chunks.PersistentChunkTicket;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import me.tayebyassine.homestead.util.minecraft.players.PlayerBank;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Paginated claimed chunks manager for a region.
 *
 * <p>Lists all claimed chunks with options to teleport (right-click), toggle force-load
 * (shift+left-click), and unclaim (left-click). Shows world-specific icons and chunk
 * metadata.</p>
 */
public final class ClaimedChunksMenu {
    private static final String MENU_KEY = "region_claimed_chunks";

    private List<RegionChunk> chunks;

    public ClaimedChunksMenu(Player player, Region region) {
        this.chunks = ChunkManager.getChunksOfRegion(region);

        PaginationMenu.builder(MENU_KEY, 9 * 5)
                .nextPageItem(MenuButtons.getNextPageButton())
                .prevPageItem(MenuButtons.getPreviousPageButton())
                .items(getItems(player, region))
                .fillEmptySlots()
                .goBack((_player, event) -> new SelectedRegionMainMenu(player, region))
                .onClick((_player, context) -> handleChunkClick(player, region, context))
                .actionButton(1, MenuButtons.getButton(MENU_KEY, 1, new Placeholder()
                        .add("{max-chunks}", Limits.getRegionLimit(region, Limits.LimitType.CHUNKS_PER_REGION))), null)
                .build()
                .open(player);
    }

    private void handleChunkClick(Player player, Region region, PaginationMenu.ClickContext context) {
        if (context.index() >= chunks.size()) return;

        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        RegionChunk chunk = chunks.get(context.index());

        if (context.event().isRightClick()) {
            handleTeleport(player, region, chunk);
        } else if (context.event().isShiftClick() && context.event().isLeftClick()) {
            handleToggleForceLoad(player, region, chunk, context);
        } else {
            handleUnclaim(player, region, chunk, context);
        }
    }

    private void handleTeleport(Player player, Region region, RegionChunk chunk) {
        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        if (!player.hasPermission("homestead.actions.regions.teleport")) {
            Messages.send(player, "common.no_permission");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        player.closeInventory();

        PlayerUtility.teleportPlayerToChunkSafely(player, chunk, null);
    }

    private void handleToggleForceLoad(Player player, Region region, RegionChunk chunk, PaginationMenu.ClickContext context) {
        if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
            Messages.send(player, "common.no_permission");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        int totalForcedLoadedChunks = ChunkManager.getChunksOfRegion(region).stream()
                .filter(RegionChunk::isForceLoaded).toList().size();
        int maxForceLoadedChunks = Limits.getRegionLimit(region, Limits.LimitType.MAX_FORCE_LOADED_CHUNKS);

        if (totalForcedLoadedChunks >= maxForceLoadedChunks && !chunk.isForceLoaded()) {
            Messages.send(player, "commands.claimlist.1");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        Homestead.getInstance().runLocationTask(chunk.toBukkitDisplayLocation(), () -> {
            Chunk bukkitChunk = chunk.toBukkit();
            if (bukkitChunk == null) return;

            boolean newState = !chunk.isForceLoaded();
            chunk.setForceLoaded(newState);

            if (newState) {
                PersistentChunkTicket.addPersistent(Homestead.getInstance(), bukkitChunk);
            } else {
                PersistentChunkTicket.removePersistent(Homestead.getInstance(), bukkitChunk);
            }

            Homestead.getInstance().runPlayerTask(player, () -> {
                PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
                chunks = ChunkManager.getChunksOfRegion(region);
                context.instance().setItems(getItems(player, region));
            });
        });
    }

    private void handleUnclaim(Player player, Region region, RegionChunk chunk, PaginationMenu.ClickContext context) {
        if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_CHUNK_UNCLAIM)) {
            Cooldown.sendCooldownMessage(player);
            return;
        }

        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        if (!player.hasPermission("homestead.actions.regions.chunks.unclaim")) {
            Messages.send(player, "common.no_permission");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        Homestead.getInstance().runLocationTask(chunk.toBukkitDisplayLocation(), () -> {
            Chunk bukkitChunk = chunk.toBukkit();
            if (bukkitChunk == null) return;

            if (!ChunkManager.isChunkClaimed(bukkitChunk) || !ChunkManager.isChunkClaimedByRegion(region, bukkitChunk)) {
                return;
            }

            if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.UNCLAIM_CHUNKS, true)) {
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            Cooldown.startCooldown(player, Cooldown.Type.REGION_CHUNK_UNCLAIM);

            ChunkUnclaimEvent unclaimEvent = new ChunkUnclaimEvent(region, bukkitChunk);
            Homestead.callEvent(unclaimEvent);
            if (unclaimEvent.isCancelled()) return;

            int before = ChunkManager.getChunksOfRegion(region).size();
            ChunkManager.unclaimChunk(region.getUniqueId(), bukkitChunk);

            if (ChunkManager.getChunksOfRegion(region).size() < before) {
                double chunkPrice = Resources.<RegionsFile>get(ResourceType.Regions).getChunkPrice();
                if (chunkPrice > 0) PlayerBank.deposit(region.getOwner(), chunkPrice);
            }

            Homestead.getInstance().runPlayerTask(player, () -> {
                PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
                ChunkBorder.show(player);
            });

            Homestead.getInstance().runPlayerTask(player, () -> {
                chunks = ChunkManager.getChunksOfRegion(region);
                context.instance().setItems(getItems(player, region));
            });
        });
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

            ButtonData data = MenuButtons.getButtonData(MENU_KEY, 0);

            World world = chunk.getWorld();

            if (data.getOriginalType().equals("CUSTOM::GETBYWORLD") && world != null) {
                data.setOriginalType(switch (world.getEnvironment()) {
                    case NETHER -> Resources.<MenusFile>get(ResourceType.Menus).get("button-types.world.nether");
                    case THE_END -> Resources.<MenusFile>get(ResourceType.Menus).get("button-types.world.the_end");
                    default -> Resources.<MenusFile>get(ResourceType.Menus).get("button-types.world.overworld");
                });
            }

            items.add(MenuButtons.getButton(data, placeholder));
        }

        return items;
    }
}

package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionTrustPlayerEvent;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public final class RegionPlayersInvited {
	private List<OfflinePlayer> invitedPlayers;

	public RegionPlayersInvited(Player player, Region region) {
		invitedPlayers = region.getInvitedPlayers();

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(10), 9 * 4,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				getItems(player, region),
				(_player, event) -> new RegionPlayersManagement(player, region),
				(_player, context) -> {
					if (context.getIndex() >= invitedPlayers.size()) return;

					OfflinePlayer invitedPlayer = invitedPlayers.get(context.getIndex());

					if (!context.getEvent().isLeftClick()) return;

					if (!region.isPlayerInvited(invitedPlayer)) return;

					region.removePlayerInvite(invitedPlayer);
					invitedPlayers = region.getInvitedPlayers();
					context.getInstance().setItems(getItems(player, region));
				});

		gui.addActionButton(0, MenuUtils.getButton(29), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.players.trust")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

				if (Resources.<RegionsFile>get(ResourceType.Regions).isInstantTrustSystemEnabled()) {
					region.removePlayerInvite(targetPlayer);
					region.addMember(targetPlayer);

					RegionTrustPlayerEvent _event = new RegionTrustPlayerEvent(region, player, targetPlayer);
					Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
				} else {
					region.addPlayerInvite(targetPlayer);

					Placeholder placeholder = new Placeholder()
							.add("{region}", region.getName())
							.add("{playername}", targetPlayer.getName())
							.add("{ownername}", region.getOwner().getName());

					Messages.send(player, 36, placeholder);

					if (targetPlayer.isOnline()) {
						Messages.send(targetPlayer.getPlayer(), 139, placeholder);
					}
				}

				RegionManager.addNewLog(region.getUniqueId(), 2, new Placeholder()
						.add("{executor}", player.getName())
						.add("{playername}", targetPlayer.getName()));
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				Homestead.getInstance().runSyncTask(() -> new RegionPlayersInvited(player, region));
			}, (message) -> {
				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

				if (target == null) {
					Messages.send(player, 29, new Placeholder().add("{playername}", message));
					return false;
				}
				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.TRUST_PLAYERS)) {
					return false;
				}
				if (region.isPlayerBanned(target)) {
					Messages.send(player, 74);
					return false;
				}
				if (region.isPlayerMember(target)) {
					Messages.send(player, 48, new Placeholder().add("{playername}", target.getName()));
					return false;
				}
				if (region.isPlayerInvited(target)) {
					Messages.send(player, 35, new Placeholder().add("{playername}", target.getName()));
					return false;
				}
				if (region.isOwner(target)) {
					Messages.send(player, 30);
					return false;
				}

				SerializableRent rent = region.getRent();
				if (rent != null && rent.getPlayerId().equals(target.getUniqueId())) {
					Messages.send(player, 196);
					return false;
				}
				if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
					Messages.send(player, 116);
					return false;
				}
				return true;
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new RegionPlayersInvited(player, region)), 75);
		});

		gui.addActionButton(2, MenuUtils.getButton(31), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;

			if (region.getInvitedPlayers().isEmpty()) {
				Messages.send(player, 76);
				return;
			}

			region.setInvitedPlayers(new ArrayList<>());
			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
			Messages.send(player, 95);
			Homestead.getInstance().runSyncTask(() -> new RegionPlayersInvited(player, region));
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (OfflinePlayer invitedPlayer : invitedPlayers) {
			items.add(MenuUtils.getButton(30, new Placeholder()
					.add("{playername}", invitedPlayer.getName())));
		}

		return items;
	}
}
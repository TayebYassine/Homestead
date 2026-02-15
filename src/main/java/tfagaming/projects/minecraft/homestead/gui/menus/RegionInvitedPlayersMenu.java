package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.sessions.playerinput.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegionInvitedPlayersMenu {
	List<OfflinePlayer> invitedPlayers;

	public RegionInvitedPlayersMenu(Player player, Region region) {
		invitedPlayers = region.getInvitedPlayers();

		PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(10), 9 * 4,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(), getItems(player, region), (_player, event) -> {
			new ManagePlayersMenu(player, region);
		}, (_player, context) -> {
			if (context.getIndex() >= invitedPlayers.size()) {
				return;
			}

			OfflinePlayer invitedPlayer = invitedPlayers.get(context.getIndex());

			if (context.getEvent().isLeftClick()) {
				if (region.isPlayerInvited(invitedPlayer)) {
					region.removePlayerInvite(invitedPlayer);

					PaginationMenu instance = context.getInstance();

					invitedPlayers = region.getInvitedPlayers();

					instance.setItems(getItems(player, region));
				}
			}
		});

		gui.addActionButton(0, MenuUtils.getButton(29), (_player, event) -> {
			if (!event.isLeftClick()) {
				return;
			}

			if (!player.hasPermission("homestead.region.players.trust")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

				if (Homestead.config.getBoolean("special-feat.ignore-trust-acceptance-system")) {
					region.removePlayerInvite(targetPlayer);

					region.addMember(targetPlayer);
				} else {
					region.addPlayerInvite(targetPlayer);

					// TODO Fix this
					// RegionsManager.addNewLog(region.getUniqueId(), 2, replacements);
				}

				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

				Homestead.getInstance().runSyncTask(() -> {
					new RegionInvitedPlayersMenu(player, region);
				});
			}, (message) -> {
				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

				if (target == null) {
					Messages.send(player, 29, new Placeholder()
							.add("{playername}", message)
					);
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
					Messages.send(player, 48, new Placeholder()
							.add("{playername}", target.getName())
					);
					return false;
				}

				if (region.isPlayerInvited(target)) {
					Messages.send(player, 35, new Placeholder()
							.add("{playername}", target.getName())
					);
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
			}, (__player) -> {
				Homestead.getInstance().runSyncTask(() -> {
					new RegionInvitedPlayersMenu(player, region);
				});
			}, 75);
		});

		gui.addActionButton(2, MenuUtils.getButton(31), (_player, event) -> {
			if (!event.isLeftClick()) {
				return;
			}

			if (region.getInvitedPlayers().isEmpty()) {
				Messages.send(player, 76);
				return;
			}

			region.setInvitedPlayers(new ArrayList<>());

			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

			Messages.send(player, 95);

			Homestead.getInstance().runSyncTask(() -> {
				new RegionInvitedPlayersMenu(player, region);
			});
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	public List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < invitedPlayers.size(); i++) {
			OfflinePlayer invitedPlayer = invitedPlayers.get(i);

			HashMap<String, String> replacements = new HashMap<>();

			replacements.put("{playername}", invitedPlayer.getName());

			items.add(MenuUtils.getButton(30, replacements));
		}

		return items;
	}
}

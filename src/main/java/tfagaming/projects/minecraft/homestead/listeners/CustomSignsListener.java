package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionTransferOwnershipEvent;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableLocation;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.platform.PlatformBridge;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;

import java.util.concurrent.TimeUnit;

public final class CustomSignsListener implements Listener {

	private static String getEventLine(SignChangeEvent event, int index) {
		return event.getLine(index) != null ? event.getLine(index) : "";
	}

	private static String getSignLine(Sign sign, int index) {
		return ChatColor.stripColor(sign.getLine(index) != null ? sign.getLine(index) : "");
	}

	private static void setEventLine(SignChangeEvent event, int index, String legacyText) {
		PlatformBridge.get().setSignLine(event, index, legacyText);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();

		String firstLine = getEventLine(event, 0).trim();

		boolean breakBlock;

		switch (firstLine.toLowerCase()) {
			case "[welcome]":
				breakBlock = handleWelcomeSign(event, player);
				break;
			case "[rent]":
				breakBlock = handleRentSign(event, player);
				break;
			case "[sell]":
				breakBlock = handleSellSign(event, player);
				break;
			default:
				return;
		}

		if (breakBlock) {
			event.getBlock().breakNaturally();
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerRightClickSign(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Player player = event.getPlayer();
		Block clickedBlock = event.getClickedBlock();

		if (!(clickedBlock.getState() instanceof Sign sign)) return;

		String firstLine = getSignLine(sign, 0).trim();

		switch (firstLine.toLowerCase()) {
			case "[welcome]": {
				event.setCancelled(true);
				Messages.send(player, 123);
				break;
			}
			case "[rent]": {
				event.setCancelled(true);
				handleRentSignInteraction(player, sign, clickedBlock);
				break;
			}
			case "[sell]": {
				event.setCancelled(true);
				handleSellSignInteraction(player, sign, clickedBlock);
				break;
			}
			default:
				break;
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onSignBreak(BlockBreakEvent event) {
		Chunk chunk = event.getBlock().getChunk();

		if (ChunkManager.isChunkClaimed(chunk) && event.getBlock() != null
				&& event.getBlock().getType().toString().toLowerCase().contains("sign")) {

			Region region = ChunkManager.getRegionOwnsTheChunk(chunk);
			if (region == null) return;

			BlockState state = event.getBlock().getState();
			Sign sign = (Sign) state;

			if (getSignLine(sign, 0).equalsIgnoreCase("[Welcome]")) {
				region.setWelcomeSign(null);
			}
		}
	}

	private boolean handleWelcomeSign(SignChangeEvent event, Player player) {
		if (!Homestead.config.isWelcomeSignEnabled()) {
			Messages.send(player, 105);
			return true;
		}

		Region region = validateRegion(player, event.getBlock().getChunk());
		if (region == null) return true;

		if (!getEventLine(event, 2).trim().isEmpty() || !getEventLine(event, 3).trim().isEmpty()) {
			Messages.send(player, 121);
			return true;
		}

		setEventLine(event, 0, ChatColor.GREEN + "[Welcome]");
		setEventLine(event, 1, ChatColor.DARK_GREEN + region.getName());
		setEventLine(event, 2, "");
		setEventLine(event, 3, "");

		region.setWelcomeSign(new SerializableLocation(event.getBlock().getLocation()));
		return false;
	}

	private boolean handleRentSign(SignChangeEvent event, Player player) {
		if (!Homestead.config.getBoolean("renting.enabled")) {
			Messages.send(player, 105);
			return true;
		}

		Region region = validateRegion(player, event.getBlock().getChunk());
		if (region == null) return true;

		String priceStr = getEventLine(event, 2).trim();

		if (!NumberUtils.isValidDouble(priceStr)) {
			Messages.send(player, 122);
			return true;
		}

		double price = Double.parseDouble(priceStr);
		double minRent = Homestead.config.getDouble("renting.min-rent");
		double maxRent = Homestead.config.getDouble("renting.max-rent");

		if (price < minRent || price > maxRent) {
			Messages.send(player, 122);
			return true;
		}

		String durationStr = getEventLine(event, 3).trim();
		long durationMs = parseDurationToMillis(durationStr);

		if (durationMs <= 0 || durationMs > 6048000000L) {
			Messages.send(player, 129);
			return true;
		}

		setEventLine(event, 0, ChatColor.GREEN + "[Rent]");
		setEventLine(event, 1, ChatColor.DARK_GREEN + region.getName());
		setEventLine(event, 2, ChatColor.RED + Formatter.getBalance(price));
		setEventLine(event, 3, ChatColor.GOLD + formatMillisToReadable(durationMs));

		return false;
	}

	private boolean handleSellSign(SignChangeEvent event, Player player) {
		if (!Homestead.config.getBoolean("selling.enabled")) {
			Messages.send(player, 105);
			return true;
		}

		Region region = validateRegion(player, event.getBlock().getChunk());
		if (region == null) return true;

		String priceStr = getEventLine(event, 2).trim();

		if (!NumberUtils.isValidDouble(priceStr)) {
			Messages.send(player, 122);
			return true;
		}

		double price = Double.parseDouble(priceStr);
		double minSell = Homestead.config.getDouble("selling.min-sell");
		double maxSell = Homestead.config.getDouble("selling.max-sell");

		if (price < minSell || price > maxSell) {
			Messages.send(player, 122);
			return true;
		}

		if (!getEventLine(event, 3).trim().isEmpty()) {
			Messages.send(player, 121);
			return true;
		}

		setEventLine(event, 0, ChatColor.GREEN + "[Sell]");
		setEventLine(event, 1, ChatColor.DARK_GREEN + region.getName());
		setEventLine(event, 2, ChatColor.RED + Formatter.getBalance(price));
		setEventLine(event, 3, "");

		return false;
	}

	private void handleRentSignInteraction(Player player, Sign sign, Block signBlock) {
		try {
			String regionName = getSignLine(sign, 1);
			String priceStr = getSignLine(sign, 2);
			double price = parseFormattedPrice(priceStr);
			String durationStr = getSignLine(sign, 3);
			long durationMs = parseFormattedDuration(durationStr);

			Region region = RegionManager.findRegion(regionName);

			if (region == null) {
				Messages.send(player, 9);
				return;
			}

			if (region.isOwner(player) || region.isPlayerBanned(player) || region.getRent() != null) {
				Messages.send(player, 30);
				return;
			}

			if (price > PlayerBank.get(player)) {
				Messages.send(player, 125);
				return;
			}

			long rentEnd = System.currentTimeMillis() + durationMs;

			PlayerBank.withdraw(player, price);
			PlayerBank.deposit(region.getOwner(), price);

			SerializableRent rent = new SerializableRent(player, price, rentEnd);

			SubArea subArea = SubAreaManager.findSubAreaHasLocationInside(signBlock.getLocation());

			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{rent-end}", Formatter.getRemainingTime(rentEnd));

			if (subArea != null) {
				subArea.setRent(rent);
				placeholder.add("{subarea}", subArea.getName());
				Messages.send(player, 194, placeholder);
			} else {
				region.setRent(rent);
				Messages.send(player, 126, placeholder);
			}

			signBlock.breakNaturally();
		} catch (NumberFormatException e) {
			Messages.send(player, 120);
		}
	}

	private void handleSellSignInteraction(Player player, Sign sign, Block signBlock) {
		try {
			String regionName = getSignLine(sign, 1);
			String priceStr = getSignLine(sign, 2);
			double price = parseFormattedPrice(priceStr);

			Region region = RegionManager.findRegion(regionName);

			if (region == null) {
				Messages.send(player, 9);
				return;
			}

			if (WarManager.isRegionInWar(region.getUniqueId())) {
				Messages.send(player, 156);
				return;
			}

			if (region.isOwner(player) || region.isPlayerBanned(player)) {
				Messages.send(player, 30);
				return;
			}

			if (price > PlayerBank.get(player)) {
				Messages.send(player, 125);
				return;
			}

			PlayerBank.withdraw(player, price);
			PlayerBank.deposit(region.getOwner(), price);

			region.setOwner(player);
			signBlock.breakNaturally();

			if (region.isPlayerMember(player)) {
				region.removeMember(player);
			}

			Messages.send(player, 124, new Placeholder()
					.add("{region}", region.getName())
					.add("{price}", Formatter.getBalance(price))
			);

			RegionTransferOwnershipEvent _event = new RegionTransferOwnershipEvent(region, player, player);
			Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
		} catch (NumberFormatException e) {
			Messages.send(player, 120);
		}
	}

	private Region validateRegion(Player player, Chunk chunk) {
		Region region = ChunkManager.getRegionOwnsTheChunk(chunk);

		if (region == null || !region.isOwner(player)) {
			Messages.send(player, 119);
			return null;
		}

		return region;
	}

	private long parseDurationToMillis(String duration) {
		if (duration == null || duration.isEmpty()) return 0;

		try {
			String numStr = duration.replaceAll("[^0-9]", "");
			if (numStr.isEmpty()) return 0;

			long num = Long.parseLong(numStr);
			char unit = duration.replaceAll("[0-9]", "").toLowerCase().charAt(0);

			return switch (unit) {
				case 's' -> num * 1000;
				case 'm' -> num * 60 * 1000;
				case 'h' -> num * 60 * 60 * 1000;
				case 'd' -> num * 24 * 60 * 60 * 1000;
				case 'w' -> num * 7 * 24 * 60 * 60 * 1000;
				default -> 0;
			};
		} catch (Exception e) {
			return 0;
		}
	}

	private String formatMillisToReadable(long millis) {
		if (millis < 1000) return millis + "ms";

		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
		long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
		long days = TimeUnit.MILLISECONDS.toDays(millis) % 7;
		long weeks = TimeUnit.MILLISECONDS.toDays(millis) / 7;

		StringBuilder sb = new StringBuilder();
		if (weeks > 0) sb.append(weeks).append(weeks == 1 ? " Week " : " Weeks ");
		if (days > 0) sb.append(days).append(days == 1 ? " Day " : " Days ");
		if (hours > 0) sb.append(hours).append(hours == 1 ? " Hour " : " Hours ");
		if (minutes > 0) sb.append(minutes).append(minutes == 1 ? " Minute " : " Minutes ");
		if (seconds > 0) sb.append(seconds).append(seconds == 1 ? " Second" : " Seconds");
		return sb.toString().trim();
	}

	// From DeepSeek (Thanks bro)
	private double parseFormattedPrice(String input) throws NumberFormatException {
		String cleanInput = input.replaceAll("[^0-9.,]", "").trim();

		if (cleanInput.matches(".*,\\d{2}$")) {
			cleanInput = cleanInput.replace(".", "").replace(",", ".");
		} else if (cleanInput.matches(".*\\.\\d{2}$")) {
			cleanInput = cleanInput.replace(",", "");
		} else {
			if (cleanInput.chars().filter(c -> c == ',' || c == '.').count() == 1) {
				cleanInput = cleanInput.replace(",", "").replace(".", "");
			}
		}

		if (input.toLowerCase().contains("k")) {
			return Double.parseDouble(cleanInput) * 1000;
		} else if (input.toLowerCase().contains("m")) {
			return Double.parseDouble(cleanInput) * 1000000;
		} else if (input.toLowerCase().contains("b")) {
			return Double.parseDouble(cleanInput) * 1000000000;
		}

		return Double.parseDouble(cleanInput);
	}

	private long parseFormattedDuration(String input) throws NumberFormatException {
		input = input.toLowerCase();

		if (input.contains("second")) {
			double seconds = Double.parseDouble(input.replaceAll("[^0-9.]", ""));
			return (long) (seconds * 1000);
		} else if (input.contains("minute")) {
			double minutes = Double.parseDouble(input.replaceAll("[^0-9.]", ""));
			return (long) (minutes * 60 * 1000);
		} else if (input.contains("hour")) {
			double hours = Double.parseDouble(input.replaceAll("[^0-9.]", ""));
			return (long) (hours * 60 * 60 * 1000);
		} else if (input.contains("day")) {
			double days = Double.parseDouble(input.replaceAll("[^0-9.]", ""));
			return (long) (days * 24 * 60 * 60 * 1000);
		} else if (input.contains("week")) {
			double weeks = Double.parseDouble(input.replaceAll("[^0-9.]", ""));
			return (long) (weeks * 7 * 24 * 60 * 60 * 1000);
		}

		return parseDurationToMillis(input);
	}
}
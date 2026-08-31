package me.tayebyassine.homestead.listeners.signs;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RegionOwnerUpdateEvent;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.gui.menus.RentConfirmationMenu;
import me.tayebyassine.homestead.managers.*;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeLocation;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.NumberUtils;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.platform.PlatformBridge;
import me.tayebyassine.homestead.util.minecraft.players.PlayerBank;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.concurrent.TimeUnit;

/**
 * Handles Homestead's custom signs: {@code [Welcome]}, {@code [Rent]} and {@code [HSell]}.
 *
 * <p>On placement, it validates ownership and formats the sign; on right-click it performs the
 * rent or purchase action, transferring ownership and funds as appropriate.</p>
 */
public final class CustomSignsListener implements Listener {

    private static final boolean ADVENTURE_SUPPORTED = PlatformBridge.isAdventureClassPresent();
    private static final long MS_PER_WEEK = 7L * 24 * 60 * 60 * 1000;

    private final SignFormatter welcomeFormatter = (event, player, region) -> {
        if (!isWelcomeSignsEnabled()) {
            Messages.send(player, "signs.0");
            return true;
        }
        if (hasContentInLines(event, 2, 3)) {
            Messages.send(player, "signs.1");
            return true;
        }
        formatSignLines(event,
                colored("[Welcome]", ChatColor.GREEN, "<green>"),
                colored(region.getName(), ChatColor.DARK_GREEN, "<dark_green>"),
                "", ""
        );
        region.setWelcomeSign(new SeLocation(event.getBlock().getLocation()));
        return false;
    };
    private final SignFormatter rentFormatter = (event, player, region) -> {
        if (!isFeatureEnabled("renting.enabled")) {
            Messages.send(player, "signs.0");
            return true;
        }

        formatSignLines(event,
                colored("[Rent]", ChatColor.GREEN, "<green>"),
                colored(region.getName(), ChatColor.DARK_GREEN, "<dark_green>")
        );
        return false;
    };
    private final SignFormatter sellFormatter = (event, player, region) -> {
        if (!isFeatureEnabled("selling.enabled")) {
            Messages.send(player, "signs.0");
            return true;
        }

        PriceValidation price = validatePrice(event, player, "selling.min-sell", "selling.max-sell");
        if (price == null) return true;

        if (hasContentInLines(event, 3)) {
            Messages.send(player, "signs.1");
            return true;
        }

        formatSignLines(event,
                colored("[HSell]", ChatColor.GREEN, "<green>"),
                colored(region.getName(), ChatColor.DARK_GREEN, "<dark_green>"),
                colored(Formatter.getBalance(price.value()), ChatColor.RED, "<red>"),
                ""
        );
        return false;
    };

    private static String colored(String text, ChatColor legacy, String adventure) {
        return (ADVENTURE_SUPPORTED ? adventure : legacy) + text;
    }

    private static void formatSignLines(SignChangeEvent event, String... lines) {
        for (int i = 0; i < lines.length && i < 4; i++) {
            PlatformBridge.get().setSignLine(event, i, lines[i]);
        }
    }

    private static boolean hasContentInLines(SignChangeEvent event, int... indices) {
        for (int index : indices) {
            if (!getEventLine(event, index).trim().isEmpty()) return true;
        }
        return false;
    }

    private static String getEventLine(SignChangeEvent event, int index) {
        return event.getLine(index) != null ? event.getLine(index) : "";
    }

    private static String getSignLine(Sign sign, int index) {
        String line = sign.getLine(index);
        return ChatColor.stripColor(line != null ? line : "");
    }

    private static RegionsFile getRegionsConfig() {
        return Resources.get(ResourceType.Regions);
    }

    private static boolean isWelcomeSignsEnabled() {
        return getRegionsConfig().isWelcomeSignEnabled();
    }

    private static boolean isFeatureEnabled(String path) {
        return getRegionsConfig().getBoolean(path);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        SignType type = SignType.fromString(getEventLine(event, 0));
        if (type == null) return;

        Player player = event.getPlayer();
        Region region = validateOwnerRegion(player, event.getBlock().getChunk());
        if (region == null) {
            event.getBlock().breakNaturally();
            return;
        }

        boolean shouldBreak = switch (type) {
            case WELCOME -> welcomeFormatter.validateAndFormat(event, player, region);
            case RENT -> rentFormatter.validateAndFormat(event, player, region);
            case SELL -> sellFormatter.validateAndFormat(event, player, region);
        };

        if (shouldBreak) {
            event.getBlock().breakNaturally();
        } else {
            Messages.send(player, "signs.7");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerRightClickSign(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (!(clickedBlock.getState() instanceof Sign sign)) return;

        SignType type = SignType.fromString(getSignLine(sign, 0));
        if (type == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        switch (type) {
            case WELCOME -> Messages.send(player, "signs.3");
            case RENT -> handleRentInteraction(player, sign, clickedBlock);
            case SELL -> handleSellInteraction(player, sign, clickedBlock);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!block.getType().name().toLowerCase().contains("sign")) return;

        Chunk chunk = block.getChunk();
        if (!ChunkManager.isChunkClaimed(chunk)) return;

        Region region = ChunkManager.getRegionOwnsTheChunk(chunk);
        if (region == null) return;

        if (block.getState() instanceof Sign sign
                && getSignLine(sign, 0).equalsIgnoreCase("[Welcome]")) {
            region.setWelcomeSign(null);
        }
    }

    private void handleRentInteraction(Player player, Sign sign, Block signBlock) {
        try {
            String regionName = getSignLine(sign, 1);

            Region region = RegionManager.findRegion(regionName);
            if (region == null || !canRent(player, region)) return;

            SeRent existingRent = region.getRent();
            SubArea subArea = SubAreaManager.findSubAreaHasLocationInside(signBlock.getLocation());
            if (subArea != null) {
                existingRent = subArea.getRent();
            }

            if (existingRent.hasRenter()) {
                Messages.send(player, "signs.17");
                return;
            }

            new RentConfirmationMenu(player, region, subArea, region.getRent());
        } catch (NumberFormatException e) {
            Messages.send(player, "signs.5");
        }
    }

    private void handleSellInteraction(Player player, Sign sign, Block signBlock) {
        if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_TRANSFER_OWNERSHIP)) {
            Cooldown.sendCooldownMessage(player);
            return;
        }

        try {
            String regionName = getSignLine(sign, 1);
            double price = parseFormattedPrice(getSignLine(sign, 2));

            Region region = RegionManager.findRegion(regionName);
            if (region == null || !canPurchase(player, region)) return;

            if (price > PlayerBank.get(player)) {
                Messages.send(player, "signs.6");
                return;
            }

            executePurchase(player, region, price, signBlock);

        } catch (NumberFormatException e) {
            Messages.send(player, "signs.5");
        }
    }

    private void executePurchase(Player player, Region region, double price, Block signBlock) {
        Cooldown.startCooldown(player, Cooldown.Type.REGION_TRANSFER_OWNERSHIP);

        PlayerBank.withdraw(player, price);
        PlayerBank.deposit(region.getOwner(), price);

        final OfflinePlayer oldOwner = region.getOwner();

        region.setOwner(player);

        signBlock.breakNaturally();

        MemberManager.removeMemberFromRegion(region.getOwner(), region);
        InviteManager.deleteInvitesOfPlayer(region, player);

        Messages.send(player, "signs.10");

        Homestead.callEvent(new RegionOwnerUpdateEvent(region, oldOwner, player));
    }

    private Region validateOwnerRegion(Player player, Chunk chunk) {
        Region region = ChunkManager.getRegionOwnsTheChunk(chunk);
        if (region == null || !region.isOwner(player)) {
            Messages.send(player, "signs.11");
            return null;
        }
        return region;
    }

    private boolean canRent(Player player, Region region) {
        if (region == null) {
            Messages.send(player, "signs.12");
            return false;
        }
        if (WarManager.isRegionInWar(region.getUniqueId())) {
            Messages.send(player, "signs.13");
            return false;
        }
        if (region.isOwner(player) || MemberManager.isMemberOfRegion(region, player) || BanManager.isBanned(region, player)) {
            Messages.send(player, "signs.14");
            return false;
        }
        return true;
    }

    private boolean canPurchase(Player player, Region region) {
        if (region == null) {
            Messages.send(player, "signs.12");
            return false;
        }
        if (WarManager.isRegionInWar(region.getUniqueId())) {
            Messages.send(player, "signs.13");
            return false;
        }
        if (region.isOwner(player) || MemberManager.isMemberOfRegion(region, player) || BanManager.isBanned(region, player)) {
            Messages.send(player, "signs.14");
            return false;
        }
        SeRent rent = region.getRent();
        if (rent != null && rent.getRenterId().equals(player.getUniqueId())) {
            Messages.send(player, "signs.15");
            return false;
        }
        return true;
    }

    private PriceValidation validatePrice(SignChangeEvent event, Player player,
                                          String minPath, String maxPath) {
        String priceStr = getEventLine(event, 2).trim();
        if (!NumberUtils.isValidDouble(priceStr)) {
            Messages.send(player, "signs.13");
            return null;
        }

        double price = Double.parseDouble(priceStr);
        double min = getRegionsConfig().getDouble(minPath);
        double max = getRegionsConfig().getDouble(maxPath);

        if (price < min || price > max) {
            Messages.send(player, "signs.5");
            return null;
        }
        return new PriceValidation(price);
    }

    private long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) return 0;
        try {
            String numStr = duration.replaceAll("[^0-9]", "");
            if (numStr.isEmpty()) return 0;

            long num = Long.parseLong(numStr);
            String units = duration.replaceAll("[0-9]", "").toLowerCase();
            if (units.isEmpty()) return 0;

            return switch (units.charAt(0)) {
                case 's' -> num * 1000;
                case 'm' -> num * 60 * 1000;
                case 'h' -> num * 60 * 60 * 1000;
                case 'd' -> num * 24 * 60 * 60 * 1000;
                case 'w' -> num * MS_PER_WEEK;
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
        appendTimeUnit(sb, weeks, "Week");
        appendTimeUnit(sb, days, "Day");
        appendTimeUnit(sb, hours, "Hour");
        appendTimeUnit(sb, minutes, "Minute");
        appendTimeUnit(sb, seconds, "Second");
        return sb.toString().trim();
    }

    private void appendTimeUnit(StringBuilder sb, long value, String unit) {
        if (value > 0) {
            sb.append(value).append(' ').append(unit);
            if (value != 1) sb.append('s');
            sb.append(' ');
        }
    }

    private double parseFormattedPrice(String input) throws NumberFormatException {
        String cleanInput = input.replaceAll("[^0-9.,]", "").trim();

        if (cleanInput.matches(".*,\\d{2}$")) {
            cleanInput = cleanInput.replace(".", "").replace(",", ".");
        } else if (cleanInput.matches(".*\\.\\d{2}$")) {
            cleanInput = cleanInput.replace(",", "");
        } else if (cleanInput.chars().filter(c -> c == ',' || c == '.').count() == 1) {
            cleanInput = cleanInput.replace(",", "").replace(".", "");
        }

        double multiplier = switch (lastCharLower(input)) {
            case 'k' -> 1_000;
            case 'm' -> 1_000_000;
            case 'b' -> 1_000_000_000;
            default -> 1;
        };

        return Double.parseDouble(cleanInput) * multiplier;
    }

    private long parseFormattedDuration(String input) throws NumberFormatException {
        input = input.toLowerCase();
        double num = Double.parseDouble(input.replaceAll("[^0-9.]", ""));

        if (input.contains("second")) return (long) (num * 1000);
        if (input.contains("minute")) return (long) (num * 60 * 1000);
        if (input.contains("hour")) return (long) (num * 60 * 60 * 1000);
        if (input.contains("day")) return (long) (num * 24 * 60 * 60 * 1000);
        if (input.contains("week")) return (long) (num * MS_PER_WEEK);

        return parseDuration(input);
    }

    private char lastCharLower(String input) {
        if (input == null || input.isEmpty()) return 0;
        return Character.toLowerCase(input.charAt(input.length() - 1));
    }

    private enum SignType {
        WELCOME("[welcome]"),
        RENT("[rent]"),
        SELL("[hsell]");

        private final String identifier;

        SignType(String identifier) {
            this.identifier = identifier;
        }

        static SignType fromString(String input) {
            String normalized = input.trim().toLowerCase();
            for (SignType type : values()) {
                if (type.identifier.equals(normalized)) return type;
            }
            return null;
        }
    }

    private interface SignFormatter {
        boolean validateAndFormat(SignChangeEvent event, Player player, Region region);
    }

    private record PriceValidation(double value) {
    }
}
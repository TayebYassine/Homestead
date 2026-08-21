package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;

import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin sub-command ({@code /hsadmin claim}) that claims a square area of chunks
 * for any region, centered on the player's position or on given chunk coordinates.
 */
public final class ClaimSubCmd extends SubCommandBuilder {

    private static final int MAX_RADIUS = 20;

    public ClaimSubCmd() {
        super("claim");
        setAdminPermission();
        setUsage("/hsadmin claim [region] [location] [radius]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 2) {
            Messages.send(player, "commands.op_claim.0", getUsage());
            return true;
        }

        Region region = RegionManager.findRegion(args[0]);

        if (region == null) {
            Messages.send(player, "commands.op_claim.1");
            return true;
        }

        World world;
        int centreChunkX;
        int centreChunkZ;
        int radius = 1;

        if (args[1].equalsIgnoreCase("here")) {
            Location loc = player.getLocation();
            world = loc.getWorld();
            centreChunkX = loc.getChunk().getX();
            centreChunkZ = loc.getChunk().getZ();

            if (args.length >= 3) {
                Integer parsedRadius = parseRadius(player, args[2]);
                if (parsedRadius == null) {
                    return true;
                }
                radius = parsedRadius;
            }
        } else {
            if (args.length < 3) {
                Messages.send(player, "commands.op_claim.0", getUsage());
                return true;
            }

            world = player.getWorld();

            try {
                centreChunkX = Integer.parseInt(args[1]);
                centreChunkZ = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                Messages.send(player, "commands.op_claim.2");
                return true;
            }

            if (args.length >= 4) {
                Integer parsedRadius = parseRadius(player, args[3]);
                if (parsedRadius == null) {
                    return true;
                }
                radius = parsedRadius;
            }
        }

        if (world == null) {
            Messages.send(player, "commands.op_claim.4");
            return true;
        }

        List<Chunk> toClaim = new ArrayList<>();
        int minCX = centreChunkX - (radius - 1);
        int maxCX = centreChunkX + (radius - 1);
        int minCZ = centreChunkZ - (radius - 1);
        int maxCZ = centreChunkZ + (radius - 1);

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                if (!world.isChunkLoaded(cx, cz)) {
                    continue;
                }

                Chunk chunk = world.getChunkAt(cx, cz);

                if (!ChunkManager.isChunkClaimed(chunk)) {
                    toClaim.add(chunk);
                }
            }
        }

        if (toClaim.isEmpty()) {
            Messages.send(player, "commands.op_claim.5");
            return true;
        }

        for (Chunk chunk : toClaim) {
            ChunkManager.claimChunk(region, chunk);
        }

        Messages.send(player, "commands.op_claim.6");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return new ArrayList<>();
        }

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(RegionManager.getRegionNames());
        } else if (args.length == 2) {
            suggestions.add("here");
            suggestions.add(String.valueOf(player.getLocation().getChunk().getX()));
        } else if (args.length == 3 && !args[1].equalsIgnoreCase("here")) {
            suggestions.add(String.valueOf(player.getLocation().getChunk().getZ()));
        } else if ((args.length == 3 && args[1].equalsIgnoreCase("here")) || args.length == 4) {
            for (int i = 1; i < 6; i++) {
                suggestions.add(String.valueOf(i));
            }
        }

        return suggestions;
    }

    private Integer parseRadius(Player player, String input) {
        int radius;

        try {
            radius = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Messages.send(player, "commands.op_claim.2");
            return null;
        }

        if (radius < 1 || radius > MAX_RADIUS) {
            Messages.send(player, "commands.op_claim.3");
            return null;
        }

        return radius;
    }
}






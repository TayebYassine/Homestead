package me.tayebyassine.homestead.api.events.war;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.War;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player kill is counted toward an ownership war.
 */
public class WarKillEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final War war;
    private final Region killerRegion;
    private final Region victimRegion;
    private final OfflinePlayer killer;
    private final OfflinePlayer victim;

    public WarKillEvent(@NotNull War war, @NotNull Region killerRegion, @NotNull Region victimRegion,
                        @NotNull OfflinePlayer killer, @NotNull OfflinePlayer victim) {
        this.war = war;
        this.killerRegion = killerRegion;
        this.victimRegion = victimRegion;
        this.killer = killer;
        this.victim = victim;
    }

    public @NotNull War getWar() {
        return war;
    }

    public @NotNull Region getKillerRegion() {
        return killerRegion;
    }

    public @NotNull Region getVictimRegion() {
        return victimRegion;
    }

    public @NotNull OfflinePlayer getKiller() {
        return killer;
    }

    public @NotNull OfflinePlayer getVictim() {
        return victim;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}

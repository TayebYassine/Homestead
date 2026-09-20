package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public class InvitePlayerEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final OfflinePlayer target;

    public InvitePlayerEvent(@NotNull Region region, @NotNull OfflinePlayer target) {
        this.region = region;
        this.target = target;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @NotNull OfflinePlayer getInvitedPlayer() {
        return target;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}

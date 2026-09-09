package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;


public class RevokePlayerInviteEvent extends APIEvent {
    private final Region region;
    private final OfflinePlayer target;

    public RevokePlayerInviteEvent(@NotNull Region region, @NotNull OfflinePlayer target) {
        this.region = region;
        this.target = target;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @NotNull OfflinePlayer getInvitedPlayer() {
        return target;
    }
}

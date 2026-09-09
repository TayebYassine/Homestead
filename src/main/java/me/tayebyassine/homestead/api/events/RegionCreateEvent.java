package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;


public class RegionCreateEvent extends APIEvent {
    private final Region region;
    private final OfflinePlayer player;

    public RegionCreateEvent(@NotNull Region region, @NotNull OfflinePlayer player) {
        this.region = region;
        this.player = player;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @NotNull OfflinePlayer getPlayer() {
        return player;
    }
}

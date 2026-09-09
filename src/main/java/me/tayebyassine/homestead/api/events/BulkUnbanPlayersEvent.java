package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.jetbrains.annotations.NotNull;

public class BulkUnbanPlayersEvent extends APIEvent {
    private final Region region;

    public BulkUnbanPlayersEvent(@NotNull Region region) {
        this.region = region;
    }

    public @NotNull Region getRegion() {
        return region;
    }
}

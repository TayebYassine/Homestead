package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.jetbrains.annotations.NotNull;


public class RegionDeleteEvent extends APIEvent {
    private final Region region;

    public RegionDeleteEvent(@NotNull Region region) {
        this.region = region;
    }

    public @NotNull Region getRegion() {
        return region;
    }
}

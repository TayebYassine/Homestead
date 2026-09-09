package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.jetbrains.annotations.NotNull;

public class BulkDeleteInvitesEvent extends APIEvent {
    private final Region region;

    public BulkDeleteInvitesEvent(@NotNull Region region) {
        this.region = region;
    }

    public @NotNull Region getRegion() {
        return region;
    }
}

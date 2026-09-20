package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeRent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RentConfigUpdateEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final SubArea subArea;
    private final SeRent rent;
    private final String field;
    private final Object oldValue;
    private final Object newValue;

    public RentConfigUpdateEvent(@NotNull Region region, @Nullable SubArea subArea, @NotNull SeRent rent,
                                 @NotNull String field, @Nullable Object oldValue, @Nullable Object newValue) {
        this.region = region;
        this.subArea = subArea;
        this.rent = rent;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @Nullable SubArea getSubArea() {
        return subArea;
    }

    public @NotNull SeRent getRent() {
        return rent;
    }

    public @NotNull String getField() {
        return field;
    }

    public @Nullable Object getOldValue() {
        return oldValue;
    }

    public @Nullable Object getNewValue() {
        return newValue;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}

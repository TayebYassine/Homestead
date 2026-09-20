package me.tayebyassine.homestead.api.events.economy;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


/**
 * Fired when money is deposited into a region's bank.
 */
public class BankDepositEvent extends me.tayebyassine.homestead.api.events.APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final double amount;

    public BankDepositEvent(@NotNull Region region, double amount) {
        this.region = region;
        this.amount = amount;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public double getAmount() {
        return amount;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}

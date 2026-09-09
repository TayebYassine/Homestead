package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.jetbrains.annotations.NotNull;


public class BankDepositEvent extends APIEvent {
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
}

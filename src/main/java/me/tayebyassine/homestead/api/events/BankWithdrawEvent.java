package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.jetbrains.annotations.NotNull;


public class BankWithdrawEvent extends APIEvent {
    private final Region region;
    private final double amount;

    public BankWithdrawEvent(@NotNull Region region, double amount) {
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

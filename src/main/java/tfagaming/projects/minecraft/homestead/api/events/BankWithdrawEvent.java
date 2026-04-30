package tfagaming.projects.minecraft.homestead.api.events;

import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


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

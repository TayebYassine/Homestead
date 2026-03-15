package tfagaming.projects.minecraft.homestead.tools.minecraft.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import java.time.Duration;

final class AdventurePlatformBridge implements IPlatformBridge {

	private static Component deserialize(String legacyText) {
		return LegacyComponentSerializer.legacySection().deserialize(legacyText);
	}

	@Override
	public void sendActionBar(Player player, String legacyText) {
		player.sendActionBar(deserialize(legacyText));
	}

	@Override
	public void showTitle(Player player, String title, String subtitle,
						  int fadeInTicks, int stayTicks, int fadeOutTicks) {
		player.showTitle(Title.title(
				deserialize(title),
				deserialize(subtitle),
				Title.Times.times(
						Duration.ofMillis(fadeInTicks  * 50L),
						Duration.ofMillis(stayTicks    * 50L),
						Duration.ofMillis(fadeOutTicks * 50L)
				)
		));
	}

	@Override
	public void sendMessage(Player player, String legacyText) {
		player.sendMessage(deserialize(legacyText));
	}

	@Override
	public void setSignLine(SignChangeEvent event, int index, String legacyText) {
		event.line(index, deserialize(legacyText));
	}
}
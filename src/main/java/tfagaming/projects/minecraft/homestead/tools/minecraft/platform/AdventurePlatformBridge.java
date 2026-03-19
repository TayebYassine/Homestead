package tfagaming.projects.minecraft.homestead.tools.minecraft.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;

import java.time.Duration;
final class AdventurePlatformBridge implements IPlatformBridge {

	private static final MiniMessage MM = MiniMessage.miniMessage();

	private static Component deserialize(String text) {
		return MM.deserialize(ColorTranslator.legacyToMiniMessage(text));
	}

	@Override
	public void sendActionBar(Player player, String text) {
		player.sendActionBar(deserialize(text));
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
	public void sendMessage(Player player, String text) {
		player.sendMessage(deserialize(text));
	}

	@Override
	public void setSignLine(SignChangeEvent event, int index, String text) {
		event.line(index, deserialize(text));
	}
}
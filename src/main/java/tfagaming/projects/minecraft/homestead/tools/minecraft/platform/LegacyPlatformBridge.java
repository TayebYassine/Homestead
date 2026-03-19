package tfagaming.projects.minecraft.homestead.tools.minecraft.platform;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;

final class LegacyPlatformBridge implements IPlatformBridge {

	private static String toLegacy(String text) {
		return ColorTranslator.translate(text);
	}

	@Override
	public void sendActionBar(Player player, String text) {
		player.spigot().sendMessage(
				ChatMessageType.ACTION_BAR,
				new TextComponent(toLegacy(text))
		);
	}

	@Override
	public void showTitle(Player player, String title, String subtitle,
						  int fadeInTicks, int stayTicks, int fadeOutTicks) {
		player.sendTitle(toLegacy(title), toLegacy(subtitle), fadeInTicks, stayTicks, fadeOutTicks);
	}

	@Override
	public void sendMessage(Player player, String text) {
		player.sendMessage(toLegacy(text));
	}

	@Override
	public void setSignLine(SignChangeEvent event, int index, String text) {
		event.setLine(index, toLegacy(text));
	}
}
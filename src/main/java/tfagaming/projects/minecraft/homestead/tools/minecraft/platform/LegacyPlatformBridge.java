package tfagaming.projects.minecraft.homestead.tools.minecraft.platform;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

final class LegacyPlatformBridge implements IPlatformBridge {

	@Override
	public void sendActionBar(Player player, String legacyText) {
		player.spigot().sendMessage(
				ChatMessageType.ACTION_BAR,
				new TextComponent(legacyText)
		);
	}

	@Override
	public void showTitle(Player player, String title, String subtitle,
						  int fadeInTicks, int stayTicks, int fadeOutTicks) {
		player.sendTitle(title, subtitle, fadeInTicks, stayTicks, fadeOutTicks);
	}

	@Override
	public void sendMessage(Player player, String legacyText) {
		player.sendMessage(legacyText);
	}

	@Override
	public void setSignLine(SignChangeEvent event, int index, String legacyText) {
		event.setLine(index, legacyText);
	}
}
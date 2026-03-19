package tfagaming.projects.minecraft.homestead.tools.minecraft.platform;

import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Abstraction layer for API calls that exist on Paper (Adventure) but differ
 * on Spigot.
 */
public interface IPlatformBridge {

	void sendActionBar(Player player, String text);

	void showTitle(Player player, String title, String subtitle,
				   int fadeInTicks, int stayTicks, int fadeOutTicks);

	void sendMessage(Player player, String text);

	void setSignLine(SignChangeEvent event, int index, String text);
}
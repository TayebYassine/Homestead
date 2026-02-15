package tfagaming.projects.minecraft.homestead.tools.minecraft.players;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class PlayerSound {
	public enum PredefinedSound {
		SUCCESS(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f),
		DENIED(Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f),
		CLICK(Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f),
		TELEPORT(Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

		private Sound sound;
		private float volume, pitch;

		PredefinedSound(Sound sound, float volume, float pitch) {
			this.sound = sound;
			this.volume = volume;
			this.pitch = pitch;
		}

		public Sound getSound() {
			return sound;
		}

		public float getVolume() {
			return volume;
		}

		public float getPitch() {
			return pitch;
		}
	}

	public static void play(Player player, PredefinedSound sound) {
		player.playSound(player.getLocation(), sound.getSound(), sound.getVolume(), sound.getPitch());
	}

	public static void play(Player player, Sound sound) {
		player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
	}
}

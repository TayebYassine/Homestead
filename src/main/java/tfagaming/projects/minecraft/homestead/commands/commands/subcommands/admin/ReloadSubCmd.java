package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import org.bukkit.command.CommandSender;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.config.ConfigLoader;
import tfagaming.projects.minecraft.homestead.config.LanguageLoader;
import tfagaming.projects.minecraft.homestead.config.MenusConfigLoader;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.validator.YAMLValidator;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ReloadSubCmd extends SubCommandBuilder {
	public ReloadSubCmd() {
		super("reload");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Homestead instance = Homestead.getInstance();

		try {
			Homestead.config = new ConfigLoader(instance);

			Homestead.language = new LanguageLoader(instance, Homestead.config.getString("language", "en-US"));

			Homestead.menusConfig = new MenusConfigLoader(instance);

			Set<String> skipKeys = new HashSet<>();

			YAMLValidator configValidator = new YAMLValidator("config.yml", new File(instance.getDataFolder(), "config.yml"),
					skipKeys);

			if (!configValidator.validate()) {
				configValidator.fix();

				Homestead.config = new ConfigLoader(instance);
			}

			YAMLValidator languageValidator = new YAMLValidator("en-US.yml", Homestead.language.getLanguageFile(Homestead.config.getString("language", "en-US")));

			if (!languageValidator.validate()) {
				languageValidator.fix();

				Homestead.language = new LanguageLoader(instance, Homestead.config.getString("language", "en-US"));
			}

			YAMLValidator menusConfigValidator = new YAMLValidator("menus.yml", new File(instance.getDataFolder(), "menus.yml"),
					skipKeys);

			if (!menusConfigValidator.validate()) {
				menusConfigValidator.fix();

				Homestead.menusConfig = new MenusConfigLoader(instance);
			}

			PlayerUtils.sendMessage(sender, 90);
		} catch (IOException e) {
			Logger.error(e);
			PlayerUtils.sendMessage(sender, 87);
		}

		return true;
	}
}

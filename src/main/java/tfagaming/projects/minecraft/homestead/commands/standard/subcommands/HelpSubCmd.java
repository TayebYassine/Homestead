package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.List;

public class HelpSubCmd extends SubCommandBuilder {
	private static final int COMMANDS_PER_PAGE = 6;

	public HelpSubCmd() {
		super("help");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/hs help [page]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		LanguageFile lang = Resources.<LanguageFile>get(ResourceType.Language);
		List<String> commandKeys = lang.getKeysUnderPath("command-descriptions");

		if (commandKeys.isEmpty()) {
			Messages.send(player, "commands.help.0");
			return true;
		}

		int totalPages = (int) Math.ceil((double) commandKeys.size() / COMMANDS_PER_PAGE);
		int page = 1;

		if (args.length > 0) {
			try {
				page = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				return true;
			}
		}

		if (page < 1) page = 1;
		if (page > totalPages) page = totalPages;

		int start = (page - 1) * COMMANDS_PER_PAGE;
		int end = Math.min(start + COMMANDS_PER_PAGE, commandKeys.size());

		Messages.send(player, "commands.help.header", page, totalPages);

		for (int i = start; i < end; i++) {
			String key = commandKeys.get(i);
			String description = lang.getString("command-descriptions." + key);
			String commandLabel = key.replace("_", " ");

			String entry = lang.getString("commands.help.entry-format")
					.replace("{command}", commandLabel)
					.replace("{description}", description);

			Messages.sendString(player, entry, "");
		}

		boolean hasPrev = page > 1;
		boolean hasNext = page < totalPages;

		String prevTag = hasPrev
				? "<click:run_command:/hs help " + (page - 1) + "><hover:show_text:'" + lang.getString("commands.help.prev-hover") + "'>" + lang.getString("commands.help.prev") + "</hover></click>"
				: lang.getString("commands.help.prev-disabled");

		String nextTag = hasNext
				? "<click:run_command:/hs help " + (page + 1) + "><hover:show_text:'" + lang.getString("commands.help.next-hover") + "'>" + lang.getString("commands.help.next") + "</hover></click>"
				: lang.getString("commands.help.next-disabled");

		String footer = lang.getString("commands.help.footer")
				.replace("{previous}", prevTag)
				.replace("{next}", nextTag);

		Messages.sendString(player, footer, "");

		return true;
	}
}
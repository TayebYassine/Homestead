package tfagaming.projects.minecraft.homestead.commands.brigadier;

import me.lucko.commodore.Commodore;
import tfagaming.projects.minecraft.homestead.Homestead;

public final class BrigadierCommands {
	private final Homestead plugin;
	private final Commodore commodore;

	public BrigadierCommands(Homestead plugin, Commodore commodore) {
		this.plugin = plugin;
		this.commodore = commodore;

		registerRegionCommand();
		registerSimpleCommands();
		registerAdminCommand();
	}

	private void registerRegionCommand() {
		BrigadierCommandBuilder.create("region")
				.literalSub("auto").end()
				.literalSub("banlist").end()
				.literalSub("claimlist").end()
				.literalSub("help").end()
				.literalSub("home").end()
				.literalSub("levels").end()
				.literalSub("logs").end()
				.literalSub("members").end()
				.literalSub("menu").end()
				.literalSub("mergeaccept").end()
				.literalSub("rewards").end()
				.literalSub("top").end()
				.literalSub("fly").end()
				.literalSub("storage").end()

				.literalSub("accept")
				.stringArg("region")
				.end()
				.literalSub("create")
				.stringArg("name")
				.end()
				.literalSub("deny")
				.stringArg("region")
				.end()
				.literalSub("merge")
				.stringArg("region")
				.end()
				.literalSub("player")
				.stringArg("player")
				.end()
				.literalSub("rename")
				.stringArg("newname")
				.end()
				.literalSub("trust")
				.stringArg("player")
				.end()
				.literalSub("unban")
				.stringArg("player")
				.end()
				.literalSub("untrust")
				.stringArg("player")
				.end()

				.literalSub("balance")
				.stringArg("region")
				.end()
				.literalSub("info")
				.stringArg("region")
				.end()
				.literalSub("rate")
				.stringArg("region")
				.end()

				.literalSub("ban")
				.stringArg("player")
				.end()

				.literalSub("kick")
				.stringArg("player")
				.end()

				.literalSub("chat")
				.greedyStringArg("message")
				.end()

				.literalSub("delete")
				.stringArg("confirm")
				.end()

				.literalSub("leave")
				.stringArg("confirm")
				.end()

				.literalSub("deposit")
				.intArg("amount", 0, 1000000000)
				.end()
				.literalSub("withdraw")
				.intArg("amount", 0, 1000000000)
				.end()

				.literalSub("mail")
				.stringArg("region")
				.greedyStringArg("message")
				.end()

				.literalSub("visit")
				.stringArg("name")
				.intArg("index", 0, 32767)
				.end()

				.literalSub("kick")
				.stringArg("player")
				.end()

				.literalSub("borders")
				.literalSub("stop")
				.endNested()
				.end()

				.literalSub("flags")
				.literalSub("global")
				.stringArg("flag")
				.stringArg("state")
				.endNested()
				.literalSub("world")
				.stringArg("flag")
				.stringArg("state")
				.endNested()
				.literalSub("member")
				.stringArg("player")
				.stringArg("flag")
				.stringArg("state")
				.endNested()
				.end()

				.literalSub("set")
				.literalSub("description")
				.greedyStringArg("description")
				.endNested()
				.literalSub("displayname")
				.greedyStringArg("displayname")
				.endNested()
				.literalSub("icon")
				.stringArg("icon")
				.endNested()
				.literalSub("mapcolor")
				.stringArg("color")
				.endNested()
				.literalSub("spawn")
				.endNested()
				.literalSub("target")
				.stringArg("region")
				.endNested()
				.literalSub("tax")
				.intArg("amount", 0, 1000000000)
				.endNested()
				.end()

				.literalSub("subareas")
				.literalSub("create")
				.stringArg("name")
				.endNested()
				.literalSub("delete")
				.stringArg("subarea")
				.endNested()
				.literalSub("rename")
				.stringArg("subarea")
				.stringArg("newname")
				.endNested()
				.literalSub("flags")
				.stringArg("subarea")
				.stringArg("flag")
				.stringArg("state")
				.endNested()
				.literalSub("players")
				.stringArg("subarea")
				.stringArg("player")
				.literalSub("add")
				.endNested()
				.literalSub("remove")
				.endNested()
				.literalSub("flags")
				.stringArg("flag")
				.stringArg("state")
				.endNested()
				.endNested()
				.end()

				.literalSub("war")
				.literalSub("declare")
				.stringArg("region")
				.intArg("prize", 0, 1000000000)
				.greedyStringArg("name")
				.endNested()
				.literalSub("surrender")
				.endNested()
				.literalSub("info")
				.endNested()
				.end()

				.register(plugin, commodore);
	}

	private void registerSimpleCommands() {
		BrigadierCommandBuilder.create("claim")
				.literalSub("radius")
				.intArg("length", 1, 10)
				.end()
				.register(plugin, commodore);

		BrigadierCommandBuilder.create("unclaim")
				.register(plugin, commodore);
	}

	private void registerAdminCommand() {
		BrigadierCommandBuilder.create("homesteadadmin")
				.literalSub("plugin").end()
				.literalSub("reload").end()
				.literalSub("updates").end()

				.literalSub("import")
				.stringArg("plugin")
				.end()
				.literalSub("export")
				.stringArg("provider")
				.end()

				.literalSub("transfer")
				.stringArg("region")
				.stringArg("player")
				.end()

				.literalSub("flagsoverride")
				.literalSub("global")
				.stringArg("flag")
				.stringArg("state")
				.endNested()
				.literalSub("world")
				.stringArg("flag")
				.stringArg("state")
				.endNested()
				.literalSub("member")
				.stringArg("player")
				.stringArg("flag")
				.stringArg("state")
				.endNested()
				.end()

				.literalSub("claim")
				.stringArg("region")
				.literalSub("here")
				.intArg("radius", 1, 20)
				.endNested()
				.end()

				.literalSub("unclaim")
				.stringArg("region")
				.literalSub("here")
				.intArg("radius", 1, 20)
				.endNested()
				.end()

				.register(plugin, commodore);
	}
}
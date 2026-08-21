package me.tayebyassine.homestead.commands.brigadier;

import me.lucko.commodore.Commodore;
import me.tayebyassine.homestead.commands.brigadier.builder.BrigadierCommandBuilder;

/**
 * Registers the Brigadier command trees that mirror the plugin's Bukkit commands.
 *
 * <p>Registration is a no-op on server software that does not support Brigadier, since
 * {@link BrigadierCommands} is only instantiated when support is detected.</p>
 */
public final class BrigadierCommands {

    private final Commodore commodore;

    /**
     * Creates the Brigadier command trees.
     *
     * @param commodore the Commodore instance used to register the trees
     */
    public BrigadierCommands(Commodore commodore) {
        this.commodore = commodore;

        registerRegionCommand();
        registerDefaultCommands();
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
                .greedyStringArg("reason")
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
                .intArg("amount", 0, Integer.MAX_VALUE)
                .end()
                .literalSub("withdraw")
                .intArg("amount", 0, Integer.MAX_VALUE)
                .end()

                .literalSub("mail")
                .stringArg("region")
                .greedyStringArg("message")
                .end()

                .literalSub("visit")
                .stringArg("name")
                .intArg("index", 0, 32767)
                .end()

                .literalSub("settime")
                .stringArg("time")
                .end()
                .literalSub("setweather")
                .stringArg("weather")
                .end()
                .literalSub("setdescription")
                .greedyStringArg("description")
                .end()
                .literalSub("setdisplayname")
                .greedyStringArg("displayname")
                .end()
                .literalSub("setmapcolor")
                .stringArg("color")
                .end()
                .literalSub("setmapicon")
                .stringArg("icon")
                .end()
                .literalSub("setmembertax")
                .intArg("amount", 0, Integer.MAX_VALUE)
                .end()
                .literalSub("setspawn")
                .end()
                .literalSub("set")
                .stringArg("region")
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

                .literalSub("subareas")
                .literalSub("create")
                .stringArg("name")
                .endNested()
                .literalSub("conf")
                .stringArg("subarea")
                .literalSub("delete")
                .endNested()
                .literalSub("rename")
                .stringArg("newname")
                .endNested()
                .literalSub("resize")
                .endNested()
                .literalSub("flags")
                .stringArg("flag")
                .stringArg("state")
                .endNested()
                .literalSub("players")
                .literalSub("add")
                .stringArg("player")
                .endNested()
                .literalSub("remove")
                .stringArg("player")
                .endNested()
                .literalSub("flags")
                .stringArg("player")
                .stringArg("flag")
                .stringArg("state")
                .endNested()
                .endNested()
                .endNested()
                .end()

                .literalSub("war")
                .literalSub("declare")
                .stringArg("region")
                .intArg("prize", 0, Integer.MAX_VALUE)
                .greedyStringArg("name")
                .endNested()
                .literalSub("surrender")
                .endNested()
                .literalSub("info")
                .endNested()
                .end()

                .register(commodore);
    }

    private void registerDefaultCommands() {
        BrigadierCommandBuilder.create("claim")
                .literalSub("radius")
                .intArg("length", 1, 10)
                .end()
                .register(commodore);

        BrigadierCommandBuilder.create("unclaim")
                .register(commodore);
    }

    private void registerAdminCommand() {
        BrigadierCommandBuilder.create("homesteadadmin")
                .literalSub("updates")
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

                .register(commodore);
    }
}

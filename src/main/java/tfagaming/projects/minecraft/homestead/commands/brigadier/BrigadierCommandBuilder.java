package tfagaming.projects.minecraft.homestead.commands.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.commodore.Commodore;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.ArrayList;
import java.util.List;

public final class BrigadierCommandBuilder {
	private final String commandName;
	private final LiteralArgumentBuilder<Object> rootBuilder;
	private final List<SubCommandBuilder> subCommands = new ArrayList<>();

	private BrigadierCommandBuilder(String commandName) {
		this.commandName = commandName;
		this.rootBuilder = LiteralArgumentBuilder.literal(commandName);
	}

	public static BrigadierCommandBuilder create(String commandName) {
		return new BrigadierCommandBuilder(commandName);
	}

	public SubCommandBuilder literalSub(String name) {
		SubCommandBuilder sub = new SubCommandBuilder(this, name);
		subCommands.add(sub);
		return sub;
	}

	public void register(Plugin plugin, Commodore commodore) {
		for (SubCommandBuilder sub : subCommands) {
			rootBuilder.then(sub.build());
		}

		LiteralCommandNode<?> commandNode = rootBuilder.build();

		PluginCommand pluginCommand = Bukkit.getServer().getPluginCommand(commandName);

		if (pluginCommand != null) {
			commodore.register(pluginCommand, commandNode);
		}
	}

	public static class SubCommandBuilder {
		private final BrigadierCommandBuilder parent;
		private final String name;
		private final LiteralArgumentBuilder<Object> builder;
		private final List<ArgumentNode> arguments = new ArrayList<>();
		private final List<SubCommandBuilder> nestedSubs = new ArrayList<>();
		private SubCommandBuilder parentSub; // For nested subcommands

		private SubCommandBuilder(BrigadierCommandBuilder parent, String name) {
			this.parent = parent;
			this.name = name;
			this.builder = LiteralArgumentBuilder.literal(name);
		}

		public SubCommandBuilder stringArg(String name) {
			arguments.add(new ArgumentNode(name, StringArgumentType.string()));
			return this;
		}

		public SubCommandBuilder greedyStringArg(String name) {
			arguments.add(new ArgumentNode(name, StringArgumentType.greedyString()));
			return this;
		}

		public SubCommandBuilder intArg(String name) {
			arguments.add(new ArgumentNode(name, IntegerArgumentType.integer()));
			return this;
		}

		public SubCommandBuilder intArg(String name, int min, int max) {
			arguments.add(new ArgumentNode(name, IntegerArgumentType.integer(min, max)));
			return this;
		}

		public SubCommandBuilder literalSub(String name) {
			SubCommandBuilder nested = new SubCommandBuilder(this.parent, name);
			nested.parentSub = this;
			nestedSubs.add(nested);
			return nested;
		}

		public BrigadierCommandBuilder end() {
			return parent;
		}

		public SubCommandBuilder endNested() {
			return parentSub;
		}

		private LiteralArgumentBuilder<Object> build() {
			if (!arguments.isEmpty()) {
				RequiredArgumentBuilder<Object, ?> current = null;

				for (int i = arguments.size() - 1; i >= 0; i--) {
					ArgumentNode arg = arguments.get(i);
					RequiredArgumentBuilder<Object, ?> argBuilder =
							(RequiredArgumentBuilder<Object, ?>) RequiredArgumentBuilder.argument(arg.name, arg.type);

					if (current == null && !nestedSubs.isEmpty()) {
						for (SubCommandBuilder nested : nestedSubs) {
							argBuilder.then(nested.build());
						}
					}

					if (current != null) {
						argBuilder.then(current);
					}

					current = argBuilder;
				}

				if (current != null) {
					builder.then(current);
				}
			} else {
				for (SubCommandBuilder nested : nestedSubs) {
					builder.then(nested.build());
				}
			}

			return builder;
		}
	}

	private static class ArgumentNode {
		private final String name;
		private final ArgumentType<?> type;

		private ArgumentNode(String name, ArgumentType<?> type) {
			this.name = name;
			this.type = type;
		}
	}
}
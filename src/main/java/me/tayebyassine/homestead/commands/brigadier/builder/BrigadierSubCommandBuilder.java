package me.tayebyassine.homestead.commands.brigadier.builder;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A single literal node (and its optional arguments and nested literals) of a Brigadier command tree.
 *
 * <p>Instances are obtained through {@link BrigadierCommandBuilder#literalSub(String)}.
 */
public final class BrigadierSubCommandBuilder {

    private final BrigadierCommandBuilder parent;
    private final LiteralArgumentBuilder<Object> builder;

    private final List<ArgumentNode> arguments = new ArrayList<>();
    private final List<BrigadierSubCommandBuilder> nestedSubs = new ArrayList<>();
    private BrigadierSubCommandBuilder parentSub;

    BrigadierSubCommandBuilder(BrigadierCommandBuilder parent, String name) {
        this.parent = parent;
        this.builder = LiteralArgumentBuilder.literal(name);
    }

    /**
     * Appends a free-form string argument.
     *
     * @param name the argument name
     * @return this node, for chaining
     */
    public BrigadierSubCommandBuilder stringArg(String name) {
        arguments.add(new ArgumentNode(name, StringArgumentType.string()));
        return this;
    }

    /**
     * Appends a greedy string argument (consumes the remainder of the input).
     *
     * @param name the argument name
     * @return this node, for chaining
     */
    public BrigadierSubCommandBuilder greedyStringArg(String name) {
        arguments.add(new ArgumentNode(name, StringArgumentType.greedyString()));
        return this;
    }

    /**
     * Appends an integer argument.
     *
     * @param name the argument name
     * @return this node, for chaining
     */
    public BrigadierSubCommandBuilder intArg(String name) {
        arguments.add(new ArgumentNode(name, IntegerArgumentType.integer()));
        return this;
    }

    /**
     * Appends an integer argument restricted to the given range (inclusive).
     *
     * @param name the argument name
     * @param min  the minimum allowed value
     * @param max  the maximum allowed value
     * @return this node, for chaining
     */
    public BrigadierSubCommandBuilder intArg(String name, int min, int max) {
        arguments.add(new ArgumentNode(name, IntegerArgumentType.integer(min, max)));
        return this;
    }

    /**
     * Appends a nested literal to this node.
     *
     * @param name the literal name
     * @return the nested node
     */
    public BrigadierSubCommandBuilder literalSub(String name) {
        BrigadierSubCommandBuilder nested = new BrigadierSubCommandBuilder(parent, name);
        nested.parentSub = this;
        nestedSubs.add(nested);
        return nested;
    }

    /**
     * Ends the current chain and returns the root {@link BrigadierCommandBuilder}.
     *
     * @return the root builder
     */
    public BrigadierCommandBuilder end() {
        return parent;
    }

    /**
     * Ends a nested literal and returns its parent node.
     *
     * @return the parent node
     */
    public BrigadierSubCommandBuilder endNested() {
        return parentSub;
    }

    LiteralArgumentBuilder<Object> build() {
        if (arguments.isEmpty()) {
            for (BrigadierSubCommandBuilder nested : nestedSubs) {
                builder.then(nested.build());
            }
            return builder;
        }

        RequiredArgumentBuilder<Object, ?> current = null;

        for (int i = arguments.size() - 1; i >= 0; i--) {
            ArgumentNode arg = arguments.get(i);
            RequiredArgumentBuilder<Object, ?> argBuilder =
                    RequiredArgumentBuilder.argument(arg.name, arg.type);

            if (current == null && !nestedSubs.isEmpty()) {
                for (BrigadierSubCommandBuilder nested : nestedSubs) {
                    argBuilder.then(nested.build());
                }
            }

            if (current != null) {
                argBuilder.then(current);
            }

            current = argBuilder;
        }

        builder.then(current);
        return builder;
    }

    /**
     * Holds an argument name and its Brigadier {@link ArgumentType}.
     */
    private static final class ArgumentNode {
        private final String name;
        private final ArgumentType<?> type;

        private ArgumentNode(String name, ArgumentType<?> type) {
            this.name = name;
            this.type = type;
        }
    }
}

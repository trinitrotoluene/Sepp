package is.trinit.sepp.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class CommandRegistry {
    public static LiteralArgumentBuilder<MessageCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<MessageCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static final LiteralArgumentBuilder<MessageCommandSource> latexNode = literal("latex")
            .then(
                    argument("text", greedyString())
                            .executes(MathCommands::latex)
            );

    public static void register(CommandDispatcher<MessageCommandSource> dispatcher) {
        var root = dispatcher.getRoot();
        dispatcher.register(latexNode);
    }
}

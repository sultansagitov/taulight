package net.result.main.commands.taulight;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ReactionsCommands {

    public static void register(CommandRegistry registry) {
        registry.register(
                new CommandInfo("react", "Add a reaction to a message", "Reactions", ReactionsCommands::react)
        );
        registry.register(
                new CommandInfo("unreact", "Remove a reaction from a message", "Reactions", ReactionsCommands::unreact)
        );
    }

    private static void react(@NotNull List<String> args, ConsoleContext context) {
        if (args.size() < 2) {
            System.out.println("Usage: react <messageID> <package:name>");
            return;
        }

        UUID messageId = UUID.fromString(args.get(0));

        String reaction = args.get(1);

        ReactionsRunner.react(context, messageId, reaction);
    }

    private static void unreact(@NotNull List<String> args, ConsoleContext context) {
        if (args.size() < 2) {
            System.out.println("Usage: unreact <messageID> <package:name>");
            return;
        }


        UUID messageId = UUID.fromString(args.get(0));

        String reaction = args.get(1);

        ReactionsRunner.unreact(context, messageId, reaction);
    }
}

package net.result.main.commands;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
public class ConsoleReactionsCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("react", ConsoleReactionsCommands::react);
        commands.put("unreact", ConsoleReactionsCommands::unreact);
    }

    private static void react(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.size() < 2) {
            System.out.println("Usage: react <messageID> <package:name>");
            return;
        }

        UUID messageId = UUID.fromString(args.get(0));

        String reaction = args.get(1);

        ConsoleReactionsRunner.react(context, messageId, reaction);
    }

    private static void unreact(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.size() < 2) {
            System.out.println("Usage: unreact <messageID> <package:name>");
            return;
        }


        UUID messageId = UUID.fromString(args.get(0));

        String reaction = args.get(1);

        ConsoleReactionsRunner.unreact(context, messageId, reaction);
    }
}

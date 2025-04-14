package net.result.main.commands;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
public class ConsoleReactionsCommands {
    public static void register(Map<String, ConsoleSandnodeCommands.LoopCondition> commands) {
        commands.put("react", ConsoleReactionsCommands::react);
        commands.put("unreact", ConsoleReactionsCommands::unreact);
    }

    private static boolean react(@NotNull List<String> args, ConsoleContext context) {
        if (args.size() < 2) {
            System.out.println("Usage: react <messageID> <package:name>");
            return false;
        }

        UUID messageId;
        String reaction;

        try {
            messageId = UUID.fromString(args.get(0));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid message ID or reaction format: " + e.getMessage());
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: react <messageID> <package:name>");
            return false;
        }

        try {
            reaction = args.get(1);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: react <messageID> <package:name>");
            return false;
        }

        ConsoleReactionsRunner.react(context, messageId, reaction);

        return false;
    }

    private static boolean unreact(@NotNull List<String> args, ConsoleContext context) {
        if (args.size() < 2) {
            System.out.println("Usage: unreact <messageID> <package:name>");
            return false;
        }

        UUID messageId;

        try {
            messageId = UUID.fromString(args.get(0));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid message ID or reaction format: " + e.getMessage());
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: unreact <messageID> <package:name>");
            return false;
        }

        String reaction;

        try {
            reaction = args.get(1);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: unreact <messageID> <package:name>");
            return false;
        }

        ConsoleReactionsRunner.unreact(context, messageId, reaction);

        return false;
    }
}

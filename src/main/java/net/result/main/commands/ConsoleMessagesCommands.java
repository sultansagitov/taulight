package net.result.main.commands;

import net.result.sandnode.exception.UnprocessedMessagesException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("SameReturnValue")
public class ConsoleMessagesCommands {
    public static void register(Map<String, ConsoleSandnodeCommands.LoopCondition> commands) {
        commands.put("messages", ConsoleMessagesCommands::messages);
        commands.put("reply", ConsoleMessagesCommands::reply);
    }

    private static boolean messages(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        UUID chatID;

        try {
            chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided: " + e.getMessage());
            return false;
        }

        if (chatID == null) {
            System.out.println("Chat not select");
            return false;
        }

        ConsoleMessagesRunner.messages(context, chatID);

        return false;
    }

    private static boolean reply(List<String> args, ConsoleContext context) {
        if (context.currentChat == null) {
            System.out.println("chat not selected");
            return false;
        }

        if (args.isEmpty()) {
            return false;
        }

        String firstArg = args.get(0);
        int replyCount;
        try {
            replyCount = Integer.parseInt(firstArg);
        } catch (NumberFormatException e) {
            System.out.printf("%s is not a number%n", firstArg);
            return false;
        }

        Set<UUID> replies = new HashSet<>();

        for (int i = 1; i <= replyCount && i < args.size(); i++) {
            try {
                replies.add(UUID.fromString(args.get(i)));
            } catch (IllegalArgumentException e) {
                System.out.printf("Invalid UUID: %s%n", args.get(i));
                return false;
            }
        }

        String input = String.join(" ", args.subList(replyCount + 1, args.size()));

        if (input.isEmpty()) {
            System.out.println("Message content is empty");
            return false;
        }

        ConsoleMessagesRunner.reply(context, input, replies);

        return false;
    }
}

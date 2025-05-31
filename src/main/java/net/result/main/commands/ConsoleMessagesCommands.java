package net.result.main.commands;

import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("SameReturnValue")
public class ConsoleMessagesCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("messages", ConsoleMessagesCommands::messages);
        commands.put("reply", ConsoleMessagesCommands::reply);
    }

    private static void messages(@NotNull List<String> args, ConsoleContext context) throws Exception {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not select");
            return;
        }

        ConsoleMessagesRunner.messages(context, chatID);
    }

    private static void reply(List<String> args, ConsoleContext context) throws Exception {
        if (context.currentChat == null) {
            System.out.println("chat not selected");
            return;
        }

        if (args.isEmpty()) {
            return;
        }

        String firstArg = args.get(0);
        int replyCount = Integer.parseInt(firstArg);

        Set<UUID> replies = new HashSet<>();

        for (int i = 1; i <= replyCount && i < args.size(); i++) {
            replies.add(UUID.fromString(args.get(i)));
        }

        String input = String.join(" ", args.subList(replyCount + 1, args.size()));

        if (input.isEmpty()) {
            System.out.println("Message content is empty");
            return;
        }

        ConsoleMessagesRunner.reply(context, input, replies);
    }
}

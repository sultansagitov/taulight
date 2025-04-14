package net.result.main.commands;

import net.result.sandnode.exception.UnprocessedMessagesException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
public class ConsoleCodesCommands {
    public static void register(Map<String, ConsoleSandnodeCommands.LoopCondition> commands) {
        commands.put("checkCode", ConsoleCodesCommands::checkCode);
        commands.put("useCode", ConsoleCodesCommands::useCode);
        commands.put("channelCodes", ConsoleCodesCommands::channelCodes);
        commands.put("myCodes", ConsoleCodesCommands::myCodes);
    }

    private static boolean checkCode(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        if (args.isEmpty()) {
            System.out.println("Usage: checkCode <code>");
            return false;
        }

        String code;

        try {
            code = args.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No code provided.");
            return false;
        }

        ConsoleCodesRunner.checkCode(context, code);

        return false;
    }

    private static boolean useCode(List<String> args, ConsoleContext context)
            throws UnprocessedMessagesException, InterruptedException {
        if (args.isEmpty()) {
            System.out.println("Usage: useCode <code>");
            return false;
        }

        String code;

        try {
            code = args.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No code provided.");
            return false;
        }

        ConsoleCodesRunner.useCode(context, code);

        return false;
    }

    private static boolean channelCodes(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        UUID chatID;

        try {
            chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);
        } catch (IllegalArgumentException e) {
            System.out.printf("Invalid UUID: %s%n", args.get(0));
            return false;
        }

        if (chatID == null) {
            System.out.println("Chat not selected");
            return false;
        }

        ConsoleCodesRunner.channelCodes(context, chatID);

        return false;
    }

    private static boolean myCodes(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        ConsoleCodesRunner.myCodes(context);
        return false;
    }
}

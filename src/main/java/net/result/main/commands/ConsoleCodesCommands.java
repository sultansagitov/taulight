package net.result.main.commands;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
public class ConsoleCodesCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("checkCode", ConsoleCodesCommands::checkCode);
        commands.put("useCode", ConsoleCodesCommands::useCode);
        commands.put("groupCodes", ConsoleCodesCommands::groupCodes);
        commands.put("myCodes", ConsoleCodesCommands::myCodes);
    }

    private static void checkCode(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.isEmpty()) {
            System.out.println("Usage: checkCode <code>");
            return;
        }

        String code = args.get(0);

        ConsoleCodesRunner.checkCode(context, code);
    }

    private static void useCode(List<String> args, ConsoleContext context) throws Exception {
        if (args.isEmpty()) {
            System.out.println("Usage: useCode <code>");
            return;
        }

        String code = args.get(0);

        ConsoleCodesRunner.useCode(context, code);
    }

    private static void groupCodes(@NotNull List<String> args, ConsoleContext context) throws Exception {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return;
        }

        ConsoleCodesRunner.groupCodes(context, chatID);
    }

    private static void myCodes(List<String> ignored, ConsoleContext context) throws Exception {
        ConsoleCodesRunner.myCodes(context);
    }
}

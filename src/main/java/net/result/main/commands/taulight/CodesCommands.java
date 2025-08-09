package net.result.main.commands.taulight;

import net.result.main.commands.ConsoleContext;
import net.result.main.commands.LoopCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
public class CodesCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("checkCode", CodesCommands::checkCode);
        commands.put("useCode", CodesCommands::useCode);
        commands.put("groupCodes", CodesCommands::groupCodes);
        commands.put("myCodes", CodesCommands::myCodes);
    }

    private static void checkCode(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.isEmpty()) {
            System.out.println("Usage: checkCode <code>");
            return;
        }

        String code = args.get(0);

        CodesRunner.checkCode(context, code);
    }

    private static void useCode(List<String> args, ConsoleContext context) throws Exception {
        if (args.isEmpty()) {
            System.out.println("Usage: useCode <code>");
            return;
        }

        String code = args.get(0);

        CodesRunner.useCode(context, code);
    }

    private static void groupCodes(@NotNull List<String> args, ConsoleContext context) throws Exception {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return;
        }

        CodesRunner.groupCodes(context, chatID);
    }

    private static void myCodes(List<String> ignored, ConsoleContext context) throws Exception {
        CodesRunner.myCodes(context);
    }
}

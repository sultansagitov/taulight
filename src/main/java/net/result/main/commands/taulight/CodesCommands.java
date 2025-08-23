package net.result.main.commands.taulight;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class CodesCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo("checkCode", "Check if a code is valid", "Codes", CodesCommands::checkCode));
        registry.register(new CommandInfo("useCode", "Redeem a code", "Codes", CodesCommands::useCode));
        registry.register(new CommandInfo("groupCodes", "List group codes", "Codes", CodesCommands::groupCodes));
        registry.register(new CommandInfo("myCodes", "List your codes", "Codes", CodesCommands::myCodes));
    }

    private static void checkCode(@NotNull List<String> args, ConsoleContext context) {
        if (args.isEmpty()) {
            System.out.println("Usage: checkCode <code>");
            return;
        }

        String code = args.get(0);

        CodesRunner.checkCode(context, code);
    }

    private static void useCode(List<String> args, ConsoleContext context) {
        if (args.isEmpty()) {
            System.out.println("Usage: useCode <code>");
            return;
        }

        String code = args.get(0);

        CodesRunner.useCode(context, code);
    }

    private static void groupCodes(@NotNull List<String> args, ConsoleContext context) {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return;
        }

        CodesRunner.groupCodes(context, chatID);
    }

    private static void myCodes(List<String> ignored, ConsoleContext context) {
        CodesRunner.myCodes(context);
    }
}

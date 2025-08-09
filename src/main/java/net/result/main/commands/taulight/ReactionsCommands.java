package net.result.main.commands.taulight;

import net.result.main.commands.ConsoleContext;
import net.result.main.commands.LoopCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
public class ReactionsCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("react", ReactionsCommands::react);
        commands.put("unreact", ReactionsCommands::unreact);
    }

    private static void react(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.size() < 2) {
            System.out.println("Usage: react <messageID> <package:name>");
            return;
        }

        UUID messageId = UUID.fromString(args.get(0));

        String reaction = args.get(1);

        ReactionsRunner.react(context, messageId, reaction);
    }

    private static void unreact(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.size() < 2) {
            System.out.println("Usage: unreact <messageID> <package:name>");
            return;
        }


        UUID messageId = UUID.fromString(args.get(0));

        String reaction = args.get(1);

        ReactionsRunner.unreact(context, messageId, reaction);
    }
}

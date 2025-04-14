package net.result.main.commands;

import net.result.taulight.chain.sender.ReactionClientChain;

import java.util.UUID;

public class ConsoleReactionsRunner {
    public static void react(ConsoleContext context, UUID messageId, String reaction) {
        try {
            var chain = new ReactionClientChain(context.io);
            context.io.chainManager.linkChain(chain);

            chain.react(messageId, reaction);
            System.out.printf("Added reaction '%s' to message %s%n", reaction, messageId);

            context.io.chainManager.removeChain(chain);
        } catch (Exception e) {
            System.out.printf("Reaction failed - %s%n", e.getClass().getSimpleName());
        }
    }

    public static void unreact(ConsoleContext context, UUID messageId, String reaction) {
        try {
            var chain = new ReactionClientChain(context.io);
            context.io.chainManager.linkChain(chain);

            chain.unreact(messageId, reaction);
            System.out.printf("Removed reaction '%s' from message %s%n", reaction, messageId);

            context.io.chainManager.removeChain(chain);
        } catch (Exception e) {
            System.out.printf("Reaction failed - %s%n", e.getClass().getSimpleName());
        }
    }
}

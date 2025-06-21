package net.result.main.commands;

import net.result.taulight.chain.sender.ReactionRequestClientChain;

import java.util.UUID;

public class ConsoleReactionsRunner {
    public static void react(ConsoleContext context, UUID messageId, String reaction) throws Exception {
        var chain = new ReactionRequestClientChain(context.client);
        context.io.chainManager.linkChain(chain);

        chain.react(messageId, reaction);
        System.out.printf("Added reaction '%s' to message %s%n", reaction, messageId);

        context.io.chainManager.removeChain(chain);
    }

    public static void unreact(ConsoleContext context, UUID messageId, String reaction) throws Exception {
        var chain = new ReactionRequestClientChain(context.client);
        context.io.chainManager.linkChain(chain);

        chain.unreact(messageId, reaction);
        System.out.printf("Removed reaction '%s' from message %s%n", reaction, messageId);

        context.io.chainManager.removeChain(chain);
    }
}

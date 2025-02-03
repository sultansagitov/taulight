package net.result.taulight.exception;

import net.result.sandnode.exception.SandnodeException;

public class ChatNotFoundException extends SandnodeException {
    public ChatNotFoundException(String chatID) {
        super("Chat with id %s not found".formatted(chatID));
    }
}

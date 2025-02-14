package net.result.taulight.exception;

import net.result.sandnode.exception.SandnodeException;

import java.util.UUID;

public class ChatNotFoundException extends SandnodeException {
    public ChatNotFoundException(UUID chatID) {
        super("Chat with id %s not found".formatted(chatID));
    }
}

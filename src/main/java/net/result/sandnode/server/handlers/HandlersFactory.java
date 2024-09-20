package net.result.sandnode.server.handlers;

import net.result.sandnode.messages.util.MessageType;
import org.jetbrains.annotations.NotNull;

public class HandlersFactory {
    public static @NotNull IProtocolHandler getHandler(@NotNull MessageType type) {
        switch (type) {
            case ERROR -> {
            }
            case WARNING -> {
            }
            case INFO -> {
            }
            case HANDSHAKE -> {
            }
            case HAPPY -> {
            }
            case MESSAGE -> {
                return new MessageHandler();
            }
            case PUBLICKEY -> {
            }
            case EXIT -> {
                return new EXITHandler();
            }
        }
        return null;
    }
}

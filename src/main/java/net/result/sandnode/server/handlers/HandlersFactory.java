package net.result.sandnode.server.handlers;

import net.result.sandnode.messages.util.MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HandlersFactory {
    public static @Nullable IProtocolHandler getHandler(@NotNull MessageType type) {
        switch (type) {
            case ERROR, WARNING, INFO, HAPPY -> {
            }
            case HANDSHAKE -> {
                return HandshakeHandler.getInstance();
            }
            case MESSAGE -> {
                return MessageHandler.getInstance();
            }
            case PUBLICKEY -> {
                return RsaPublicKeyHandler.getInstance();
            }
            case EXIT -> {
                return ExitHandler.getInstance();
            }
            case TMPONLINE -> {
                return OnlineHandler.getInstance();
            }
            case FORWARD -> {
                return ForwardHandler.getInstance();
            }
            case SYMKEY -> {
                return SymKeyHandler.getInstance();
            }
        }
        return null;
    }
}

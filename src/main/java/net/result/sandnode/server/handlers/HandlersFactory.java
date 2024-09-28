package net.result.sandnode.server.handlers;

import net.result.sandnode.messages.util.MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HandlersFactory {
    public static @Nullable IProtocolHandler getHandler(@NotNull MessageType type) {
        switch (type) {
            case ERROR -> {
            }
            case WARNING -> {
            }
            case INFO -> {
            }
            case HANDSHAKE -> {
                return new HandshakeHandler();
            }
            case HAPPY -> {
            }
            case MESSAGE -> {
                return new MessageHandler();
            }
            case PUBLICKEY -> {
                return RsaPublicKeyHandler.getInstance();
            }
            case EXIT -> {
                return new ExitHandler();
            }
            case TMPONLINE -> {
                return new OnlineHandler();
            }
            case FORWARD -> {
                return new ForwardHandler();
            }
        }
        return null;
    }
}

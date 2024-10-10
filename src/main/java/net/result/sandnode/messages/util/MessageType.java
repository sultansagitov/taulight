package net.result.sandnode.messages.util;

import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.server.handlers.*;
import org.jetbrains.annotations.NotNull;

public enum MessageType {
    ERROR((byte) 0),
    WARNING((byte) 1),
    INFO((byte) 2),
    HANDSHAKE((byte) 3) {
        @Override
        public IProtocolHandler handler() {
            return HandshakeHandler.getInstance();
        }
    },
    HAPPY((byte) 4),
    MESSAGE((byte) 5) {
        @Override
        public IProtocolHandler handler() {
            return MessageHandler.getInstance();
        }
    },
    PUBLICKEY((byte) 6) {
        @Override
        public IProtocolHandler handler() {
            return RsaPublicKeyHandler.getInstance();
        }
    },
    EXIT((byte) 7) {
        @Override
        public IProtocolHandler handler() {
            return ExitHandler.getInstance();
        }
    },
    FORWARD((byte) 8) {
        @Override
        public IProtocolHandler handler() {
            return ForwardHandler.getInstance();
        }
    },
    SYMKEY((byte) 9) {
        @Override
        public IProtocolHandler handler() {
            return SymKeyHandler.getInstance();
        }
    },
    TMPONLINE((byte) 10) {
        @Override
        public IProtocolHandler handler() {
            return OnlineHandler.getInstance();
        }
    };


    private final byte type;

    MessageType(byte type) {
        this.type = type;
    }

    public int asByte() {
        return type;
    }

    public IProtocolHandler handler() {
        return null;
    }

    public static @NotNull MessageType getMessageType(byte type) throws NoSuchReqHandler {
        for (MessageType messageType : values())
            if (messageType.asByte() == type)
                return messageType;
        throw new NoSuchReqHandler(type);
    }
}

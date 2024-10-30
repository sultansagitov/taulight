package net.result.sandnode.messages.util;

import net.result.sandnode.exceptions.NoSuchReqHandler;
import org.jetbrains.annotations.NotNull;

public enum MessageType {
    ERR(0),
    REQ(1),
    PUB(2),
    SYM(3),
    EXT(4),
    MSG(5),
    TMPFORWARD(7),
    TMPONLINE(8);


    private final byte type;

    MessageType(int type) {
        this.type = (byte) type;
    }

    public static @NotNull MessageType getMessageType(byte type) throws NoSuchReqHandler {
        for (MessageType messageType : values())
            if (messageType.asByte() == type)
                return messageType;
        throw new NoSuchReqHandler(type);
    }

    public int asByte() {
        return type;
    }
}

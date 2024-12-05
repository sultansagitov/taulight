package net.result.sandnode.messages.util;

import net.result.sandnode.exceptions.NoSuchMessageTypeException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public enum MessageTypes implements IMessageType {
    ERR(0),
    WARN(1),
    PUB(2),
    SYM(3),
    REQ(4),
    EXIT(5),
    REG(6),
    LOGIN(7),
    TOKEN(8),
    GROUP(9);

    private static final List<IMessageType> list = new ArrayList<>();

    static {
        for (MessageTypes m : MessageTypes.values()) {
            MessageTypes.register(m);
        }
    }

    private final byte type;

    MessageTypes(int i) {
        this.type = (byte) i;
    }

    public static @NotNull IMessageType getMessageType(byte type) throws NoSuchMessageTypeException {
        for (IMessageType messageType : list) {
            if (messageType.asByte() == type) {
                return messageType;
            }
        }
        throw new NoSuchMessageTypeException(type);
    }

    public static void register(IMessageType messageType) {
        list.removeIf(m -> messageType.asByte() == m.asByte());
        list.add(messageType);
    }

    @Override
    public int asByte() {
        return type;
    }
}

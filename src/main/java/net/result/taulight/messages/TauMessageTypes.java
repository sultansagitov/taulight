package net.result.taulight.messages;

import net.result.sandnode.messages.util.IMessageType;
import net.result.sandnode.messages.util.MessageTypes;

public enum TauMessageTypes implements IMessageType {
    ONL(10),
    ECHO(11),
    FWD(12);

    private final byte type;

    TauMessageTypes(int i) {
        this.type = (byte) i;
    }

    public static void registerAll() {
        for (TauMessageTypes m : TauMessageTypes.values()) {
            MessageTypes.register(m);
        }
    }

    @Override
    public int asByte() {
        return type;
    }
}

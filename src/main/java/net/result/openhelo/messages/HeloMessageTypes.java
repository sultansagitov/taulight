package net.result.openhelo.messages;

import net.result.sandnode.messages.util.IMessageType;
import net.result.sandnode.messages.util.MessageTypes;

public enum HeloMessageTypes implements IMessageType {
    ONL(6),
    ONL_RES(7),
    ECH(8),
    FWD(9);

    private final byte type;

    HeloMessageTypes(int i) {
        this.type = (byte) i;
    }

    public static void registerAll() {
        for (HeloMessageTypes m : HeloMessageTypes.values()) {
            MessageTypes.register(m);
        }
    }

    @Override
    public int asByte() {
        return type;
    }
}

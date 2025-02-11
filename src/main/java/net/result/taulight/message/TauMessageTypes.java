package net.result.taulight.message;

import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypeManager;

import java.util.Arrays;

public enum TauMessageTypes implements MessageType {
    CHAT(16),
    DIRECT(17),
    CHANNEL(18),
    FWD_REQ(19),
    FWD(20);

    private final byte type;

    TauMessageTypes(int i) {
        this.type = (byte) i;
    }

    public static void registerAll() {
        Arrays.stream(TauMessageTypes.values()).forEach(m -> MessageTypeManager.instance().add(m));
    }

    @Override
    public int asByte() {
        return type;
    }
}

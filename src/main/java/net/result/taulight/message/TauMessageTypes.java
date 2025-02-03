package net.result.taulight.message;

import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypeManager;

import java.util.Arrays;

public enum TauMessageTypes implements MessageType {
    FWD(12),
    TAULIGHT(13),
    CHANNEL(14),
    FWD_REQ(15);

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

package net.result.taulight.messages;

import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.messages.util.MessageTypeManager;

import java.util.Arrays;

public enum TauMessageTypes implements MessageType {
    ONL(10),
    ECHO(11),
    FWD(12),
    TAULIGHT(13);

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

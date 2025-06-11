package net.result.taulight.message;

import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypeManager;

import java.util.Arrays;

public enum TauMessageTypes implements MessageType {
    CHAT(16),
    DIALOG(17),
    GROUP(18),
    FWD_REQ(19),
    FWD(20),
    MESSAGE(21),
    MEMBERS(22),
    CHECK_CODE(23),
    USE_CODE(24),
    REACTION(25),
    ROLES(26),
    TAU_SETTINGS(27),
    MESSAGE_FILE(28);

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

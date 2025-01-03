package net.result.sandnode.messages.util;

public enum MessageTypes implements MessageType {
    ERR(0),
    WARN(1),
    PUB(2),
    SYM(3),
    EXIT(5),
    REG(6),
    LOGIN(7),
    TOKEN(8),
    GROUP(9),
    HAPPY(50),
    CHAIN_NAME(86);

    private final byte type;

    MessageTypes(int i) {
        this.type = (byte) i;
    }

    @Override
    public int asByte() {
        return type;
    }
}

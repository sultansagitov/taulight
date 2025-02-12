package net.result.sandnode.message.util;

public enum MessageTypes implements MessageType {
    CHAIN_NAME(0),
    ERR(1),
    WARN(2),
    EXIT(3),
    HAPPY(4),
    PUB(5),
    SYM(6),
    REG(7),
    LOG_PASSWD(8),
    LOGIN(9),
    GROUP(10);

    private final byte type;

    MessageTypes(int i) {
        this.type = (byte) i;
    }

    @Override
    public int asByte() {
        return type;
    }
}

package net.result.sandnode.message.util;

public enum MessageTypes implements MessageType {
    CHAIN_NAME(0),
    ERR(1),
    EXIT(2),
    HAPPY(3),
    PUB(4),
    SYM(5),
    REG(6),
    LOG_PASSWD(7),
    LOGIN(8),
    GROUP(9);

    private final byte type;

    MessageTypes(int i) {
        this.type = (byte) i;
    }

    @Override
    public int asByte() {
        return type;
    }
}

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
    LOGOUT(9),
    GROUP(10),
    WHOAMI(11),
    NAME(12),
    FILE(13),
    DEK(14);

    private final byte type;

    MessageTypes(int i) {
        this.type = (byte) i;
    }

    @Override
    public int asByte() {
        return type;
    }
}

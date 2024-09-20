package net.result.sandnode.messages.util;

public enum MessageType {
    ERROR((byte) 0),
    WARNING((byte) 1),
    INFO((byte) 2),
    HANDSHAKE((byte) 3),
    HAPPY((byte) 4),
    MESSAGE((byte) 5),
    PUBLICKEY((byte) 6),
    EXIT((byte) 7);

    private final byte type;

    MessageType(byte type) {
        this.type = type;
    }

    public int getByte() {
        return type;
    }
}

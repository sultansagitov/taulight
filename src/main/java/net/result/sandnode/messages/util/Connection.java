package net.result.sandnode.messages.util;

import org.jetbrains.annotations.NotNull;

public enum Connection {
    SERVER2SERVER(NodeType.SERVER, NodeType.SERVER),
    SERVER2CLIENT(NodeType.SERVER, NodeType.CLIENT),
    CLIENT2SERVER(NodeType.CLIENT, NodeType.SERVER),
    CLIENT2CLIENT(NodeType.CLIENT, NodeType.CLIENT);

    private final NodeType from;
    private final NodeType to;

    Connection(NodeType from, NodeType to) {
        this.from = from;
        this.to = to;
    }

    public NodeType getFrom() {
        return from;
    }

    public NodeType getTo() {
        return to;
    }

    public @NotNull Connection getOpposite() {
        return switch (this) {
            case SERVER2SERVER -> SERVER2SERVER;
            case SERVER2CLIENT -> CLIENT2SERVER;
            case CLIENT2SERVER -> SERVER2CLIENT;
            case CLIENT2CLIENT -> CLIENT2CLIENT;
        };
    }

    public static @NotNull Connection fromString(@NotNull String string) {
        String from = string.split("2")[0];
        String to = string.split("2")[1];
        if (from.equals("SERVER")) {
            if (to.equals("SERVER"))
                return SERVER2SERVER;
            else return SERVER2CLIENT;
        } else {
            if (to.equals("SERVER"))
                return CLIENT2SERVER;
            else return CLIENT2CLIENT;
        }
    }

    public static @NotNull Connection fromByte(byte b) {
        byte from = (byte) (b & 0b10000000);
        byte to = (byte) (b & 0b01000000);
        if (from == 0) {
            if (to == 0)
                return CLIENT2CLIENT;
            else
                return CLIENT2SERVER;
        } else {
            if (to == 0)
                return SERVER2CLIENT;
            else
                return SERVER2SERVER;
        }
    }

    @Override
    public @NotNull String toString() {
        return from.name() + "2" + to.name();
    }
}

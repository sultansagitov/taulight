package net.result.sandnode.messages.util;

import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.NodeType.HUB;
import static net.result.sandnode.messages.util.NodeType.USER;

public enum Connection {
    HUB2HUB(HUB, HUB),
    HUB2USER(HUB, USER),
    USER2HUB(USER, HUB),
    USER2USER(USER, USER);

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
            case HUB2HUB -> HUB2HUB;
            case HUB2USER -> USER2HUB;
            case USER2HUB -> HUB2USER;
            case USER2USER -> USER2USER;
        };
    }

    public static @NotNull Connection fromByte(byte b) {
        NodeType from = (b & 0b10000000) != 0 ? HUB : USER;
        NodeType to = (b & 0b01000000) != 0 ? HUB : USER;
        return fromType(from, to);
    }

    public static @NotNull Connection fromType(@NotNull NodeType from, @NotNull NodeType to) {
        return switch (from) {
            case USER -> (to == USER) ? USER2USER : USER2HUB;
            case HUB  -> (to == USER) ? HUB2USER  : HUB2HUB;
        };
    }

    @Override
    public @NotNull String toString() {
        return from.name() + "2" + to.name();
    }

}


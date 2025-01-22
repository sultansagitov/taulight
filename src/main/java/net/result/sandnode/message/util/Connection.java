package net.result.sandnode.message.util;

import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.message.util.NodeType.HUB;
import static net.result.sandnode.message.util.NodeType.AGENT;

public enum Connection {
    HUB2HUB(HUB, HUB),
    HUB2AGENT(HUB, AGENT),
    AGENT2HUB(AGENT, HUB),
    AGENT2AGENT(AGENT, AGENT);

    private final NodeType from;
    private final NodeType to;

    Connection(NodeType from, NodeType to) {
        this.from = from;
        this.to = to;
    }

    public static @NotNull Connection fromByte(byte b) {
        NodeType from = (b & 0b10000000) != 0 ? HUB : AGENT;
        NodeType to = (b & 0b01000000) != 0 ? HUB : AGENT;
        return fromType(from, to);
    }

    public static @NotNull Connection fromType(@NotNull NodeType from, @NotNull NodeType to) {
        return switch (from) {
            case AGENT -> (to == AGENT) ? AGENT2AGENT : AGENT2HUB;
            case HUB -> (to == AGENT) ? HUB2AGENT : HUB2HUB;
        };
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
            case HUB2AGENT -> AGENT2HUB;
            case AGENT2HUB -> HUB2AGENT;
            case AGENT2AGENT -> AGENT2AGENT;
        };
    }

    @Override
    public @NotNull String toString() {
        return from.name() + "2" + to.name();
    }

}


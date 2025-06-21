package net.result.sandnode.message.util;

import org.jetbrains.annotations.NotNull;

public enum Connection {
    HUB2HUB(NodeType.HUB, NodeType.HUB),
    HUB2AGENT(NodeType.HUB, NodeType.AGENT),
    AGENT2HUB(NodeType.AGENT, NodeType.HUB),
    AGENT2AGENT(NodeType.AGENT, NodeType.AGENT);

    private final NodeType from;
    private final NodeType to;

    Connection(NodeType from, NodeType to) {
        this.from = from;
        this.to = to;
    }

    public static @NotNull Connection fromByte(byte b) {
        NodeType from = (b & 0b10000000) != 0 ? NodeType.HUB : NodeType.AGENT;
        NodeType to = (b & 0b01000000) != 0 ? NodeType.HUB : NodeType.AGENT;
        return fromType(from, to);
    }

    public static @NotNull Connection fromType(@NotNull NodeType from, @NotNull NodeType to) {
        return switch (from) {
            case AGENT -> (to == NodeType.AGENT) ? AGENT2AGENT : AGENT2HUB;
            case HUB -> (to == NodeType.AGENT) ? HUB2AGENT : HUB2HUB;
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


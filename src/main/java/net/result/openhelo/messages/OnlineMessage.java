package net.result.openhelo.messages;

import net.result.openhelo.HeloType;

import static net.result.openhelo.HeloType.ONLINE;

public class OnlineMessage extends HeloMessage {
    public OnlineMessage() {}

    @Override
    public HeloType getType() {
        return ONLINE;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }
}

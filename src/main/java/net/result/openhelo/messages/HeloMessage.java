package net.result.openhelo.messages;

import net.result.openhelo.HeloType;

public abstract class HeloMessage {
    public abstract HeloType getType();
    public abstract byte[] toByteArray();
}

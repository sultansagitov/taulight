package net.result.openhelo.messages;

import net.result.sandnode.messages.JSONMessage;
import org.jetbrains.annotations.NotNull;

public abstract class HeloJSONMessage extends HeloMessage {
    private final JSONMessage message;

    public HeloJSONMessage(@NotNull JSONMessage message) {
        this.message = message;
    }

    protected byte @NotNull [] getJSONBytes() {
        return message.toString().getBytes();
    }
}

package net.result.openhelo.messages;

import org.jetbrains.annotations.NotNull;

public abstract class TextMessage extends HeloMessage {
    public final String data;

    public TextMessage(@NotNull String data) {
        this.data = data;
    }

    @Override
    public byte[] toByteArray() {
        return data.getBytes();
    }
}

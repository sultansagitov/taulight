package net.result.openhelo.messages;

import net.result.openhelo.HeloType;
import org.jetbrains.annotations.NotNull;

import static net.result.openhelo.HeloType.FORWARD;

public class ForwardMessage extends TextMessage {
    public ForwardMessage(@NotNull String data) {
        super(data);
    }

    @Override
    public HeloType getType() {
        return FORWARD;
    }
}

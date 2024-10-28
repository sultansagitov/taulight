package net.result.openhelo.messages;

import net.result.openhelo.HeloType;
import org.jetbrains.annotations.NotNull;

import static net.result.openhelo.HeloType.ECHO;

public class EchoMessage extends TextMessage {
    public EchoMessage(@NotNull String data) {
        super(data);
    }

    @Override
    public HeloType getType() {
        return ECHO;
    }
}

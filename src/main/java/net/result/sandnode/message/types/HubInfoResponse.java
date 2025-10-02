package net.result.sandnode.message.types;

import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class HubInfoResponse extends TextMessage {
    public HubInfoResponse(String name) {
        this(new Headers(), name);
    }

    public HubInfoResponse(@NotNull Headers headers, String name) {
        super(headers.setType(MessageTypes.HUB_INFO), name);
    }

    public HubInfoResponse(@NotNull RawMessage raw) {
        super(raw.expect(MessageTypes.HUB_INFO));
    }
}

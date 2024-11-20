package net.result.openhelo.messages;

import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.util.HeadersBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.result.openhelo.messages.HeloMessageTypes.ONL_RES;

public class OnlineResponseMessage extends Message {
    public final List<String> users;

    public OnlineResponseMessage(@NotNull HeadersBuilder builder, @NotNull List<String> users) {
        super(builder.set(ONL_RES));
        this.users = users;
    }

    public OnlineResponseMessage(@NotNull IMessage response) {
        this(response.getHeadersBuilder(), List.of(new String(response.getBody()).split(",")));
    }

    @Override
    public byte[] getBody() {
        return String.join(",", users).getBytes();
    }
}

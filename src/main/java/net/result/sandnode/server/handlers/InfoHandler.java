package net.result.sandnode.server.handlers;

import net.result.sandnode.config.ServerConfigSingleton;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.RawMessage;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class InfoHandler implements IProtocolHandler {

    @Override
    public IMessage getResponse(@NotNull IMessage request) {
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(ServerConfigSingleton.getInfoMIME())
                .set(request.getHeaders().getConnection().getOpposite());
        RawMessage response = new RawMessage(headersBuilder);
        response.setBody(ServerConfigSingleton.getInfo().getBytes(US_ASCII));
        return response;
    }

}

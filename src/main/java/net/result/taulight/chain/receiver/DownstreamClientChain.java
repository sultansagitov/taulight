package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.types.DownstreamResponse;
import net.result.taulight.util.TauAgentProtocol;

import java.util.Base64;
import java.util.UUID;

public abstract class DownstreamClientChain extends ClientChain implements ReceiverChain {
    public DownstreamClientChain(SandnodeClient client) {
        super(client);
    }

    @Override
    public Message handle(RawMessage request) {
        final var response = new DownstreamResponse(request);
        final var view = response.getServerMessage();
        final var input = view.message;

        UUID keyID = input.keyID;
        final var decrypted = keyID != null
            ? TauAgentProtocol
                .loadDEK(client, keyID)
                .decrypt(Base64.getDecoder().decode(input.content))
            : input.content;

        sendFin(new HappyMessage());

        onMessage(view, decrypted, response.isYourSession());
        return null;
    }

    public abstract void onMessage(ChatMessageViewDTO serverMessage, String decrypted, boolean yourSession);
}

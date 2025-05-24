package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.types.ForwardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Base64;

public abstract class ForwardClientChain extends ClientChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardClientChain.class);

    public ForwardClientChain(SandnodeClient client) {
        super(client);
    }

    @Override
    public void sync() throws SandnodeException {
        while (io.connected) {
            RawMessage request;
            try {
                request = queue.take();
            } catch (InterruptedException e) {
                LOGGER.info("{} is ended", this);
                break;
            }

            final ForwardResponse tfm = new ForwardResponse(request);
            ChatMessageViewDTO view = tfm.getServerMessage();
            ChatMessageInputDTO input = view.message;

            String decrypted;
            if (input.keyID != null) {
                KeyStorage keyStorage = client.clientConfig.loadDEK(input.keyID);
                decrypted = keyStorage.encryption().decrypt(Base64.getDecoder().decode(input.content), keyStorage);
            } else {
                decrypted = input.content;
            }

            onMessage(view, decrypted, tfm.isYourSession());

            if (request.headers().fin()) {
                LOGGER.info("{} ended by FIN flag in received message", toString());
                break;
            }
        }
    }

    public abstract void onMessage(ChatMessageViewDTO serverMessage, String decrypted, boolean yourSession);
}

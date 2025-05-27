package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.DEKListMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.dto.DEKDTO;
import net.result.taulight.message.types.ForwardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Base64;

public abstract class ForwardClientChain extends ClientChain implements ReceiverChain {
    public ForwardClientChain(SandnodeClient client) {
        super(client);
    }

    @Override
    public void sync() throws SandnodeException, InterruptedException {
        RawMessage res = queue.take();
        ServerErrorManager.instance().handleError(res);

        ForwardResponse response = new ForwardResponse(res);
        ChatMessageViewDTO view = response.getServerMessage();
        ChatMessageInputDTO input = view.message;

        String decrypted;
        if (input.keyID != null) {
            KeyStorage keyStorage;
            try {
                keyStorage = client.clientConfig.loadDEK(input.keyID);
            } catch (KeyStorageNotFoundException e) {
                send(Errors.KEY_NOT_FOUND.createMessage());
                RawMessage raw = queue.take();
                ServerErrorManager.instance().handleError(raw);
                DEKDTO dto = new DEKListMessage(raw).list().get(0);
                keyStorage = dto.decrypt(client);
                client.clientConfig.saveDEK(input.nickname, dto.id, keyStorage);
            }
            decrypted = keyStorage.encryption().decrypt(Base64.getDecoder().decode(input.content), keyStorage);
        } else {
            decrypted = input.content;
        }

        sendFin(new HappyMessage());

        onMessage(view, decrypted, response.isYourSession());
    }

    public abstract void onMessage(ChatMessageViewDTO serverMessage, String decrypted, boolean yourSession);
}

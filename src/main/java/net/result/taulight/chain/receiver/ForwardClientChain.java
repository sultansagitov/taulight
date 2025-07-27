package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.hubagent.Agent;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.DEKListMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.types.ForwardResponse;

import java.util.Base64;

public abstract class ForwardClientChain extends ClientChain implements ReceiverChain {
    public ForwardClientChain(SandnodeClient client) {
        super(client);
    }

    @Override
    public Message handle(RawMessage request) throws Exception {
        ForwardResponse response = new ForwardResponse(request);
        ChatMessageViewDTO view = response.getServerMessage();
        ChatMessageInputDTO input = view.message;

        String decrypted;
        if (input.keyID != null) {
            KeyStorage keyStorage;
            Agent agent = client.node().agent();
            try {
                keyStorage = agent.config.loadDEK(client.address, input.keyID);
            } catch (KeyStorageNotFoundException e) {
                var raw = sendAndReceive(new ErrorMessage(Errors.KEY_NOT_FOUND));
                var dto = new DEKListMessage(raw).list().get(0);
                var dek = dto.dek;
                keyStorage = dek.decrypt(agent.config.loadPersonalKey(client.address, dto.senderNickname));
                agent.config.saveDEK(client.address, input.nickname, dek.id, keyStorage);
            }
            decrypted = keyStorage.decrypt(Base64.getDecoder().decode(input.content));
        } else {
            decrypted = input.content;
        }

        sendFin(new HappyMessage());

        onMessage(view, decrypted, response.isYourSession());
        return null;
    }

    public abstract void onMessage(ChatMessageViewDTO serverMessage, String decrypted, boolean yourSession);
}

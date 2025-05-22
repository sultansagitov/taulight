package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.KeyDTO;
import net.result.taulight.message.types.PersonalKeyMessage;

import java.util.UUID;

public class PersonalKeyClientChain extends ClientChain {
    public PersonalKeyClientChain(SandnodeClient client) {
        super(client);
    }

    public void sendPersonalKey(String nickname, KeyDTO encryptor, KeyDTO keyStorage)
            throws InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException, 
            UnprocessedMessagesException, CryptoException, ExpectedMessageException {
        send(new PersonalKeyMessage(nickname, encryptor, keyStorage));

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        new HappyMessage(raw);
    }

    public void sendPersonalKey(String nickname, UUID encryptorID, KeyDTO key)
            throws InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException, CryptoException, ExpectedMessageException {
        KeyStorage keyStorage = client.clientConfig
                .loadMemberKey(encryptorID)
                .orElseThrow(KeyStorageNotFoundException::new);
        sendPersonalKey(nickname, new KeyDTO(encryptorID, keyStorage), key);
    }

//    public synchronized Collection<PersonalKeyDTO> getPersonalKeys() {
//        send(new PersonalKeyMessage();
//    }
}

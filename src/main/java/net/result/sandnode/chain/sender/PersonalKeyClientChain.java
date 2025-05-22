package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.PersonalKeyListMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.KeyDTO;
import net.result.taulight.dto.PersonalKeyDTO;
import net.result.taulight.message.types.PersonalKeyMessage;

import java.util.Collection;
import java.util.UUID;

public class PersonalKeyClientChain extends ClientChain {
    public PersonalKeyClientChain(SandnodeClient client) {
        super(client);
    }

    public UUID sendPersonalKey(String nickname, KeyDTO encryptor, KeyStorage keyStorage)
            throws InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException, CryptoException, DeserializationException {
        send(new PersonalKeyMessage(nickname, encryptor, keyStorage));

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        return new UUIDMessage(raw).uuid;
    }

    public UUID sendPersonalKey(String nickname, UUID encryptorID, KeyStorage keyStorage)
            throws InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException, CryptoException, ExpectedMessageException, DeserializationException {
        KeyStorage encryptor = client.clientConfig
                .loadMemberKey(encryptorID)
                .orElseThrow(KeyStorageNotFoundException::new);
        return sendPersonalKey(nickname, new KeyDTO(encryptorID, encryptor), keyStorage);
    }

    public Collection<PersonalKeyDTO> getKeys() throws UnprocessedMessagesException, InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException, DeserializationException {
        send(new PersonalKeyMessage());

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        return new PersonalKeyListMessage(raw).list();
    }
}

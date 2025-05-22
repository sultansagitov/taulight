package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.dto.KeyDTO;
import net.result.taulight.dto.PersonalKeyDTO;

public class PersonalKeyMessage extends MSGPackMessage<PersonalKeyDTO> {
    public PersonalKeyMessage(String nickname, KeyDTO encryptor, KeyDTO keyStorage)
            throws CryptoException, EncryptionException {
        super(new Headers().setType(MessageTypes.PERSONAL_KEY), new PersonalKeyDTO(nickname, encryptor, keyStorage));
    }

    public PersonalKeyMessage(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(MessageTypes.PERSONAL_KEY), PersonalKeyDTO.class);
    }

    public PersonalKeyDTO dto() {
        return object;
    }
}

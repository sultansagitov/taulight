package net.result.taulight.message.types;

import net.result.sandnode.encryption.interfaces.KeyStorage;
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

public class PersonalKeyRequest extends MSGPackMessage<PersonalKeyDTO> {
    public enum DataType {SEND_PERSONAL_KEY, GET_KEYS, GET_KEY_OF}

    public PersonalKeyRequest(DataType dataType, PersonalKeyDTO personalKeyDTO) {
        super(new Headers().setType(MessageTypes.PERSONAL_KEY).setValue("type", dataType.name()), personalKeyDTO);
    }

    public static PersonalKeyRequest sendPersonalKey(String nickname, KeyDTO encryptor, KeyStorage keyStorage)
            throws EncryptionException, CryptoException {
        return new PersonalKeyRequest(DataType.SEND_PERSONAL_KEY, new PersonalKeyDTO(nickname, encryptor, keyStorage));
    }

    public static PersonalKeyRequest getKeys() {
        return new PersonalKeyRequest(DataType.GET_KEYS, new PersonalKeyDTO());
    }

    public static PersonalKeyRequest getKeyOf(String nickname) {
        return new PersonalKeyRequest(DataType.GET_KEY_OF, new PersonalKeyDTO(nickname));
    }

    public PersonalKeyRequest(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(MessageTypes.PERSONAL_KEY), PersonalKeyDTO.class);
    }

    public DataType type() {
        return headers().getOptionalValue("type")
                .map(name -> DataType.valueOf(name.toUpperCase()))
                .orElse(DataType.GET_KEYS);
    }

    public PersonalKeyDTO dto() {
        return object;
    }
}

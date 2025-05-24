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
import net.result.taulight.dto.DEKDTO;

public class DEKRequest extends MSGPackMessage<DEKDTO> {
    public enum DataType {SEND, GET, GET_PERSONAL_KEY_OF}

    public DEKRequest(DataType dataType, DEKDTO DEKDTO) {
        super(new Headers().setType(MessageTypes.DEK).setValue("type", dataType.name()), DEKDTO);
    }

    public static DEKRequest send(String nickname, KeyDTO encryptor, KeyStorage keyStorage)
            throws EncryptionException, CryptoException {
        return new DEKRequest(DataType.SEND, new DEKDTO(nickname, encryptor, keyStorage));
    }

    public static DEKRequest get() {
        return new DEKRequest(DataType.GET, new DEKDTO());
    }

    public static DEKRequest getPersonalKeyOf(String nickname) {
        return new DEKRequest(DataType.GET_PERSONAL_KEY_OF, new DEKDTO(nickname));
    }

    public DEKRequest(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(MessageTypes.DEK), DEKDTO.class);
    }

    public DataType type() {
        return headers().getOptionalValue("type")
                .map(name -> DataType.valueOf(name.toUpperCase()))
                .orElse(DataType.GET);
    }

    public DEKDTO dto() {
        return object;
    }
}

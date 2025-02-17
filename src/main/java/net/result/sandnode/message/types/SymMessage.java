package net.result.sandnode.message.types;

import net.result.sandnode.exception.DataNotEncryptedException;
import net.result.sandnode.exception.EncryptionTypeException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.NoSuchEncryptionException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.encryption.Encryptions.NONE;
import static net.result.sandnode.message.util.MessageTypes.SYM;

public class SymMessage extends Message {
    public final SymmetricKeyStorage symmetricKeyStorage;

    public SymMessage(@NotNull IMessage message) throws ExpectedMessageException, NoSuchEncryptionException,
            EncryptionTypeException, DataNotEncryptedException {
        super(message.expect(SYM).headers());

        if (message.headersEncryption() == NONE) {
            throw new DataNotEncryptedException("Headers not encrypted");
        }
        if (message.headers().bodyEncryption() == NONE) {
            throw new DataNotEncryptedException("Body not encrypted");
        }

        SymmetricEncryption encryption =
                EncryptionManager.findSymmetric(Byte.parseByte(message.headers().getValue("encryption")));

        symmetricKeyStorage = encryption.toKeyStorage(message.getBody());
    }

    public SymMessage(@NotNull Headers headers, @NotNull SymmetricKeyStorage symmetricKeyStorage) {
        super(headers.setType(SYM).setValue("encryption", "" + symmetricKeyStorage.encryption().asByte()));
        this.symmetricKeyStorage = symmetricKeyStorage;
    }

    @Override
    public byte[] getBody() {
        return symmetricKeyStorage.toBytes();
    }
}

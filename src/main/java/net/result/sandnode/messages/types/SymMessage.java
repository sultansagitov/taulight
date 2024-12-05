package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.encryption.interfaces.ISymmetricKeyConvertor;
import net.result.sandnode.encryption.interfaces.ISymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageTypes.SYM;

public class SymMessage extends Message {
    public final ISymmetricKeyStorage symmetricKeyStorage;

    public SymMessage(@NotNull IMessage message) throws ExpectedMessageException, NoSuchEncryptionException, CannotUseEncryption {
        super(message.getHeaders());
        ExpectedMessageException.check(message, SYM);
        ISymmetricEncryption encryption =
                Encryptions.findSymmetric(Byte.parseByte(message.getHeaders().get("encryption")));

        ISymmetricKeyConvertor convertor = encryption.keyConvertor();
        symmetricKeyStorage = convertor.toKeyStorage(message.getBody());
    }

    public SymMessage(@NotNull Headers headers, @NotNull ISymmetricKeyStorage symmetricKeyStorage) {
        super(headers.set(SYM).set("encryption", "" + symmetricKeyStorage.encryption().asByte()));
        this.symmetricKeyStorage = symmetricKeyStorage;
    }

    @Override
    public byte[] getBody() {
        return symmetricKeyStorage.encryption().keyConvertor().toBytes(symmetricKeyStorage);
    }
}

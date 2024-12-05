package net.result.sandnode;

import net.result.sandnode.client.SandnodeClient;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.DecryptionException;
import net.result.sandnode.exceptions.EncryptionException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.PublicKeyResponse;
import net.result.sandnode.messages.types.RequestMessage;
import net.result.sandnode.messages.types.SymMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.encryption.interfaces.ISymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.encryption.Encryption.NONE;

public class ClientProtocol {
    public static void PUB(@NotNull SandnodeClient client) throws UnexpectedSocketDisconnectException, EncryptionException,
            KeyStorageNotFoundException, NoSuchEncryptionException, DecryptionException, NoSuchMessageTypeException,
            CannotUseEncryption, CreatingKeyException, MessageSerializationException, MessageWriteException {
        Headers headers = new Headers().set(NONE);
        IMessage request = new RequestMessage(headers);
        client.io.sendMessage(request, NONE);

        IMessage response = client.io.receiveMessage();
        PublicKeyResponse publicKeyResponse = new PublicKeyResponse(response);
        client.io.setMainKey(publicKeyResponse.keyStorage);
    }

    public static void sendSYM(@NotNull SandnodeClient client) throws KeyNotCreatedException, UnexpectedSocketDisconnectException,
            EncryptionException, KeyStorageNotFoundException, CannotUseEncryption, MessageSerializationException,
            MessageWriteException {
        ISymmetricEncryption symmetricEncryption = client.node.nodeConfig.symmetricKeyEncryption();
        IKeyStorage keyStorage = symmetricEncryption.generator().generate();

        if (!(keyStorage instanceof ISymmetricKeyStorage symmetricKeyStorage)) {
            throw new CannotUseEncryption(keyStorage.encryption());
        }

        client.node.globalKeyStorage.set(symmetricKeyStorage);

        if (!client.node.globalKeyStorage.has(client.io.getServerMainEncryption()))
            throw new KeyNotCreatedException(client.io.getServerMainEncryption());

        if (!client.node.globalKeyStorage.has(symmetricEncryption))
            throw new KeyNotCreatedException(symmetricEncryption);

        client.node.globalKeyStorage.get(client.io.getServerMainEncryption());

        Headers headers = new Headers().set(client.io.getServerMainEncryption());
        IMessage symMessage = new SymMessage(headers, symmetricKeyStorage);

        client.io.sendMessage(symMessage);
    }
}

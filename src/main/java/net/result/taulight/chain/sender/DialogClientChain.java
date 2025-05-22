package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.FileMessage;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.KeyDTO;
import net.result.taulight.message.types.DialogRequest;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DialogClientChain extends ClientChain {
    public DialogClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized UUID getDialogID(String nickname)
            throws InterruptedException, DeserializationException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(DialogRequest.getDialogID(nickname));

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        return new UUIDMessage(raw).uuid;
    }

    public synchronized @Nullable FileDTO getAvatar(UUID chatID) throws InterruptedException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException, ExpectedMessageException {
        send(DialogRequest.getAvatar(chatID));
        try {
            RawMessage raw = queue.take();
            ServerErrorManager.instance().handleError(raw);

            return new FileMessage(raw).dto();
        } catch (NoEffectException e) {
            return null;
        }
    }

    public synchronized KeyDTO getDialogKey(UUID chatID) throws UnprocessedMessagesException,
            InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException, EncryptionTypeException,
            NoSuchEncryptionException, CreatingKeyException, ExpectedMessageException, DeserializationException {
        send(DialogRequest.getKey(chatID));

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);
        PublicKeyResponse response = new PublicKeyResponse(raw);
        AsymmetricKeyStorage keyStorage = response.keyStorage;
        UUID uuid;
        try {
            uuid = UUID.fromString(response.headers().getValue("chat-id"));
        } catch (Exception e) {
            throw new DeserializationException(e);
        }
        return new KeyDTO(uuid, keyStorage);
    }
}

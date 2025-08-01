package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.FileIOUtil;
import net.result.taulight.message.types.DialogRequest;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DialogClientChain extends ClientChain {
    public DialogClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized UUID getDialogID(String nickname)
            throws InterruptedException, ProtocolException, SandnodeErrorException {
        var raw = sendAndReceive(DialogRequest.getDialogID(nickname));
        return new UUIDMessage(raw).uuid;
    }

    public synchronized @Nullable FileDTO getAvatar(UUID chatID)
            throws InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException, ExpectedMessageException, DeserializationException {
        send(DialogRequest.getAvatar(chatID));
        try {
            return FileIOUtil.receive(this::receive);
        } catch (NoEffectException e) {
            return null;
        }
    }
}

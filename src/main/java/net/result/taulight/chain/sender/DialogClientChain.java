package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.FileMessage;
import net.result.sandnode.util.IOController;
import net.result.sandnode.message.UUIDMessage;
import net.result.taulight.message.types.DialogRequest;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DialogClientChain extends ClientChain {
    public DialogClientChain(IOController io) {
        super(io);
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
}

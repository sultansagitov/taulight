package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.MemberNotFoundException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.taulight.message.types.DirectResponse;

import java.util.UUID;

public class DirectClientChain extends ClientChain {
    private final String memberID;
    public UUID chatID;

    public DirectClientChain(IOController io, String memberID) {
        super(io);
        this.memberID = memberID;
    }

    @Override
    public void sync()
            throws InterruptedException, ExpectedMessageException, DeserializationException, MemberNotFoundException {
        send(new DirectRequest(memberID));

        RawMessage raw = queue.take();

        if (raw.getHeaders().getType() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);

            if (errorMessage.error == Errors.MEMBER_NOT_FOUND) {
                throw new MemberNotFoundException();
            } else {
                throw new ImpossibleRuntimeException(errorMessage.error);
            }
        }

        DirectResponse response = new DirectResponse(raw);
        chatID = response.getChatID();
    }
}

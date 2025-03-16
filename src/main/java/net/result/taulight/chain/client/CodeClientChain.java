package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.taulight.code.InviteTauCode;
import net.result.taulight.message.types.TauCodeRequest;
import net.result.taulight.message.types.TauCodeResponse;

import java.time.ZonedDateTime;

public class CodeClientChain extends ClientChain {
    public CodeClientChain(IOController io) {
        super(io);
    }

    public InviteTauCode checkCode(String code) throws UnprocessedMessagesException, InterruptedException,
            ExpectedMessageException, UnknownSandnodeErrorException, SandnodeErrorException, DeserializationException {
        send(TauCodeRequest.check(code));
        RawMessage raw = queue.take();

        if (raw.headers().type() == MessageTypes.ERR) {
            SandnodeError error = new ErrorMessage(raw).error;
            ServerErrorManager.instance().throwAll(error);
        }

        TauCodeResponse response = new TauCodeResponse(raw);
        // TODO rewrite it
        String title = response.object.title;
        ZonedDateTime expiresData = response.object.expiresData;

        return new InviteTauCode(title, expiresData);
    }
}

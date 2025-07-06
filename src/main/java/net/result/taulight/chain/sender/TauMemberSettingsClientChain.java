package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.TauMemberSettingsResponseDTO;
import net.result.taulight.message.types.TauMemberSettingsRequest;
import net.result.taulight.message.types.TauMemberSettingsResponse;

public class TauMemberSettingsClientChain extends ClientChain {
    public TauMemberSettingsClientChain(SandnodeClient client) {
        super(client);
    }

    public TauMemberSettingsResponseDTO get() throws UnprocessedMessagesException, InterruptedException,
            DeserializationException, UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException {
        send(new TauMemberSettingsRequest());

        RawMessage raw = receive();
        ServerErrorManager.instance().handleError(raw);

        return new TauMemberSettingsResponse(raw).dto();
    }

    public TauMemberSettingsResponseDTO setShowStatus(boolean b)
            throws UnprocessedMessagesException, InterruptedException, UnknownSandnodeErrorException,
            SandnodeErrorException, ExpectedMessageException, DeserializationException {
        send(new TauMemberSettingsRequest(TauMemberSettingsRequest.SHOW_STATUS, String.valueOf(b)));

        RawMessage raw = receive();
        ServerErrorManager.instance().handleError(raw);

        return new TauMemberSettingsResponse(raw).dto();
    }
}

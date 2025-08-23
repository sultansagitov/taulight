package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.TauMemberSettingsDTO;
import net.result.taulight.message.types.TauMemberSettingsRequest;
import net.result.taulight.message.types.TauMemberSettingsResponse;

public class TauMemberSettingsClientChain extends ClientChain {
    public TauMemberSettingsClientChain(SandnodeClient client) {
        super(client);
    }

    public TauMemberSettingsDTO get() {
        var raw = sendAndReceive(new TauMemberSettingsRequest());
        return new TauMemberSettingsResponse(raw).dto();
    }

    public TauMemberSettingsDTO setShowStatus(boolean b) {
        var message = new TauMemberSettingsRequest(TauMemberSettingsRequest.SHOW_STATUS, String.valueOf(b));
        var raw = sendAndReceive(message);
        return new TauMemberSettingsResponse(raw).dto();
    }
}

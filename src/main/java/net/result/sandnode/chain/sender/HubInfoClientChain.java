package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.HubInfoDTO;
import net.result.sandnode.message.types.HubInfoRequest;
import net.result.sandnode.message.types.HubInfoResponse;
import net.result.sandnode.serverclient.SandnodeClient;

public class HubInfoClientChain extends ClientChain {
    public HubInfoClientChain(SandnodeClient client) {
        super(client);
    }

    public HubInfoDTO get(boolean requestName, boolean requestImage) {
        var raw = sendAndReceive(new HubInfoRequest(requestName, requestImage));
        String name = null;
        if (requestName) {
            name = new HubInfoResponse(raw).content();
        }
        return new HubInfoDTO(name);
    }
}

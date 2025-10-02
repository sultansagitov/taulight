package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HubInfoRequest;
import net.result.sandnode.message.types.HubInfoResponse;

public class HubInfoServerChain extends ServerChain implements ReceiverChain {
    @Override
    public HubInfoResponse handle(RawMessage raw) {
        var request = new HubInfoRequest(raw);

        HubInfoResponse nameResponse;
        if (request.name) {
            var hub = session.node().hub();
            nameResponse = new HubInfoResponse(hub.config.name());
            if (!request.image) {
                return nameResponse;
            } else {
                send(nameResponse);
            }
        }

        // TODO image

        return null;
    }
}

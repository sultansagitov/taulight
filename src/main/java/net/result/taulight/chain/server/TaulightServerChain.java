package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.types.TaulightRequest;
import net.result.taulight.message.types.TaulightResponse;
import net.result.taulight.message.types.TaulightResponse.TaulightResponseData;

public class TaulightServerChain extends ServerChain {
    public TaulightServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        while (true) {
            TaulightRequest request = new TaulightRequest(queue.take());

            if (request.getMessageType() == TaulightRequest.DataType.GET) {
                var chats = database.getChats(session.member);
                send(new TaulightResponse(TaulightResponseData.get(chats)));
            }

            if (request.getHeaders().isFin()) break;
        }
    }
}

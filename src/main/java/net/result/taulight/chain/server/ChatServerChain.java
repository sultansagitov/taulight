package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.types.ChatRequest;
import net.result.taulight.message.types.ChatResponse;

public class ChatServerChain extends ServerChain {
    public ChatServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        while (true) {
            ChatRequest request = new ChatRequest(queue.take());

            switch (request.getMessageType()) {
                case GET -> send(ChatResponse.get(database.getChats(session.member)));
            }

            if (request.getHeaders().isFin()) break;
        }
    }
}

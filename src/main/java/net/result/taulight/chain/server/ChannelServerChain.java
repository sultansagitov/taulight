package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.taulight.db.TauDatabase;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DeserializationException;
import net.result.taulight.group.TauGroupManager;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.ClientMember;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauErrors;
import net.result.taulight.group.TauChatGroup;
import net.result.taulight.message.types.ChannelRequest;
import net.result.taulight.messenger.TauChannel;
import net.result.taulight.messenger.TauChat;

import java.util.Optional;
import java.util.UUID;

public class ChannelServerChain extends ServerChain {
    public ChannelServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException {
        ChannelRequest request = new ChannelRequest(queue.take());
        ChannelRequest.DataType type = request.object.type;

        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        if (type == null) {
            sendFin(Errors.TOO_FEW_ARGS.message());
            return;
        }

        switch (type) {
            case NEW -> {
                String title = request.object.title;

                if (title == null) {
                    sendFin(Errors.TOO_FEW_ARGS.message());
                    return;
                }

                String id = UUID.randomUUID().toString();
                TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

                TauChannel channel = new TauChannel(title, id, session.member);
                database.saveChat(channel);
                database.addMemberToChat(channel, session.member);

                TauChatGroup tauChatGroup = manager.getGroup(channel);
                session.member.getSessions().forEach(s -> s.addToGroup(tauChatGroup));

                send(new HappyMessage());
            }
            case REQUEST -> {
            }
            case ADD -> {
                String id = request.object.id;
                ClientMember cMember = request.object.member;

                if (id == null || cMember == null) {
                    sendFin(Errors.TOO_FEW_ARGS.message());
                    return;
                }

                Optional<TauChat> optChat = database.getChat(id);
                Optional<Member> optMember = database.findMemberByMemberID(cMember.memberID);

                if (optChat.isEmpty()) {
                    sendFin(TauErrors.CHAT_NOT_FOUND.message());
                    return;
                }

                if (optMember.isEmpty()) {
                    sendFin(Errors.ADDRESSED_MEMBER_NOT_FOUND.message());
                    return;
                }

                Member member = optMember.get();

                if (!(optChat.get() instanceof TauChannel channel)) {
                    sendFin(Errors.WRONG_ADDRESS.message());
                    return;
                }

                if (channel.getOwner() != session.member) {
                    sendFin(Errors.UNAUTHORIZED.message());
                    return;
                }

                database.addMemberToChat(channel, member);

                send(new HappyMessage());
            }
        }
    }
}

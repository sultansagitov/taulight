package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.ChatEntity;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.db.TauMemberEntity;
import net.result.taulight.group.TauChatGroup;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.dto.ChatMemberDTO;
import net.result.taulight.message.types.MembersResponse;
import net.result.sandnode.message.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MembersServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(MembersServerChain.class);

    public MembersServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, UnprocessedMessagesException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager groupManager = (TauGroupManager) session.server.serverConfig.groupManager();

        while (true) {
            UUIDMessage request = new UUIDMessage(queue.take());

            if (session.member == null) {
                send(Errors.UNAUTHORIZED.createMessage());
                continue;
            }

            try {
                Optional<ChatEntity> optChat = database.getChat(request.uuid);

                if (optChat.isEmpty()) {
                    send(Errors.NOT_FOUND.createMessage());
                    continue;
                }

                ChatEntity chat = optChat.get();
                TauChatGroup group = groupManager.getGroup(chat);
                Collection<TauMemberEntity> members = database.getMembers(chat);

                if (!members.contains(session.member.tauMember())) {
                    send(Errors.NOT_FOUND.createMessage());
                    continue;
                }

                Map<String, ChatMemberDTO> map = members.stream()
                        .map(TauMemberEntity::member)
                        .collect(Collectors.toMap(MemberEntity::nickname, ChatMemberDTO::new));

                for (Session s : group.getSessions()) {
                    if (s.member != null) {
                        map.computeIfPresent(s.member.nickname(), (nickname, record) -> {
                            record.status = ChatMemberDTO.Status.ONLINE;
                            return record;
                        });
                    }
                }

                send(new MembersResponse(map.values()));

            } catch (DatabaseException e) {
                LOGGER.error(e);
                send(Errors.SERVER_ERROR.createMessage());
                continue;
            }

            if (request.headers().fin()) break;
        }
    }
}

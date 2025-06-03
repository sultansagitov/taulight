package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.*;
import net.result.taulight.dto.ChatMemberDTO;
import net.result.taulight.dto.MemberStatus;
import net.result.taulight.dto.RoleDTO;
import net.result.taulight.cluster.ChatCluster;
import net.result.taulight.cluster.TauClusterManager;
import net.result.taulight.message.types.MembersResponse;
import net.result.taulight.util.ChatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MembersServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(MembersServerChain.class);

    public MembersServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, UnprocessedMessagesException {
        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);
        TauClusterManager clusterManager = session.server.container.get(TauClusterManager.class);

        while (true) {
            UUIDMessage request = new UUIDMessage(queue.take());

            if (session.member == null) {
                send(Errors.UNAUTHORIZED.createMessage());
                continue;
            }

            try {
                Optional<ChatEntity> optChat = chatUtil.getChat(request.uuid);

                if (optChat.isEmpty()) {
                    send(Errors.NOT_FOUND.createMessage());
                    continue;
                }

                ChatEntity chat = optChat.get();
                ChatCluster cluster = clusterManager.getCluster(chat);

                if (!chatUtil.contains(chat, session.member.tauMember())) {
                    send(Errors.NOT_FOUND.createMessage());
                    return;
                }

                Collection<TauMemberEntity> tauMembers = chatUtil.getMembers(chat);

                Map<TauMemberEntity, List<String>> memberRolesMap = new HashMap<>();
                Set<RoleDTO> roleDTOs = null;

                if (chat instanceof ChannelEntity channel) {
                    Set<RoleDTO> set = new HashSet<>();

                    for (RoleEntity role : channel.roles()) {
                        set.add(new RoleDTO(role));

                        for (TauMemberEntity member : role.members()) {
                            memberRolesMap
                                    .computeIfAbsent(member, k -> new ArrayList<>())
                                    .add(role.id().toString());
                        }
                    }

                    roleDTOs = set;
                }

                Map<MemberEntity, ChatMemberDTO> map = new HashMap<>();
                for (TauMemberEntity m : tauMembers) {
                    List<String> roleIds = memberRolesMap.getOrDefault(m, null);
                    map.put(m.member(), new ChatMemberDTO(m, roleIds));
                }

                for (Session s : cluster.getSessions()) {
                    if (s.member != null) {
                        map.get(s.member).status = MemberStatus.ONLINE;
                    }
                }

                send(new MembersResponse(map.values(), roleDTOs));


            } catch (DatabaseException e) {
                LOGGER.error(e);
                send(Errors.SERVER_ERROR.createMessage());
                continue;
            }

            if (request.headers().fin()) break;
        }
    }
}

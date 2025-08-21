package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.cluster.ChatCluster;
import net.result.taulight.cluster.TauClusterManager;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.GroupEntity;
import net.result.taulight.entity.RoleEntity;
import net.result.taulight.entity.TauMemberEntity;
import net.result.taulight.dto.ChatMemberDTO;
import net.result.taulight.dto.MemberStatus;
import net.result.taulight.dto.RoleDTO;
import net.result.taulight.message.types.MembersResponse;
import net.result.taulight.util.ChatUtil;

import java.util.*;

public class MembersServerChain extends ServerChain implements ReceiverChain {
    @Override
    public MembersResponse handle(RawMessage raw) throws Exception {
        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);
        TauClusterManager clusterManager = session.server.container.get(TauClusterManager.class);

        UUIDMessage request = new UUIDMessage(raw);

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        Optional<ChatEntity> optChat = chatUtil.getChat(request.uuid);

        if (optChat.isEmpty()) {
            throw new NotFoundException();
        }

        ChatEntity chat = optChat.get();
        ChatCluster cluster = clusterManager.getCluster(chat);

        if (!chatUtil.contains(chat, session.member.tauMember())) {
            throw new NotFoundException();
        }

        Collection<TauMemberEntity> tauMembers = chatUtil.getMembers(chat);

        Map<TauMemberEntity, List<String>> memberRolesMap = new HashMap<>();
        Set<RoleDTO> roleDTOs = null;

        if (chat instanceof GroupEntity group) {
            Set<RoleDTO> set = new HashSet<>();

            for (RoleEntity role : group.roles()) {
                set.add(role.toDTO());

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
            map.put(m.member(), m.toChatMemberDTO(roleIds));
        }

        for (Session s : cluster.getSessions()) {
            ChatMemberDTO dto = map.get(s.member);
            if (s.member != null && dto.status != MemberStatus.HIDDEN) {
                dto.status = MemberStatus.ONLINE;
            }
        }

        return new MembersResponse(map.values(), roleDTOs);
    }
}

package net.result.taulight.util;

import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.DialogEntity;
import net.result.taulight.entity.GroupEntity;
import net.result.taulight.entity.TauMemberEntity;
import net.result.taulight.repository.GroupRepository;
import net.result.taulight.repository.TauMemberRepository;

import java.util.*;

public class ChatUtil {
    private final GroupRepository groupRepo;
    private final JPAUtil jpaUtil;
    private final TauMemberRepository tauMemberRepo;

    public ChatUtil(Container container) {
        jpaUtil = container.get(JPAUtil.class);
        groupRepo = container.get(GroupRepository.class);
        tauMemberRepo = container.get(TauMemberRepository.class);
    }

    public Optional<ChatEntity> getChat(UUID id) {
        Optional<GroupEntity> group = jpaUtil.find(GroupEntity.class, id);
        if (group.isPresent()) return group.map(c -> c);
        return jpaUtil.find(DialogEntity.class, id).map(d -> d);
    }

    public Collection<TauMemberEntity> getMembers(ChatEntity chat) {
        if (chat instanceof GroupEntity group) return group.getMembers();
        if (chat instanceof DialogEntity dialog) {
            TauMemberEntity e1 = dialog.getFirstMember();
            TauMemberEntity e2 = dialog.getSecondMember();
            if (e1.equals(e2)) {
                return Set.of(e1);
            } else {
                return Set.of(e1, e2);
            }
        }
        return Set.of();
    }

    public boolean contains(ChatEntity chat, MemberEntity member) {
        return contains(chat, tauMemberRepo.findByMember(member));
    }

    public boolean contains(ChatEntity chat, TauMemberEntity member) {
        if (chat instanceof GroupEntity group) {
            return groupRepo.contains(group, member);
        }

        if (chat instanceof DialogEntity dialog) {
            if (dialog.getFirstMember().equals(member)) return true;
            return dialog.getSecondMember().equals(member);
        }
        return false;
    }
}
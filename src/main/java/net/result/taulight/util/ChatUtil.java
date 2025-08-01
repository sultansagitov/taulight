package net.result.taulight.util;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.taulight.db.*;

import java.util.*;

public class ChatUtil {
    private final GroupRepository groupRepo;
    private final DialogRepository dialogRepo;

    public ChatUtil(Container container) {
        super();
        groupRepo = container.get(GroupRepository.class);
        dialogRepo = container.get(DialogRepository.class);
    }

    public Optional<ChatEntity> getChat(UUID id) throws DatabaseException {
        Optional<GroupEntity> group = groupRepo.findById(id);
        if (group.isPresent()) return group.map(c -> c);
        return dialogRepo.findById(id).map(d -> d);
    }

    public Collection<TauMemberEntity> getMembers(ChatEntity chat) {
        if (chat instanceof GroupEntity group) return group.members();
        if (chat instanceof DialogEntity dialog) {
            TauMemberEntity e1 = dialog.firstMember();
            TauMemberEntity e2 = dialog.secondMember();
            if (e1.equals(e2)) {
                return Set.of(e1);
            } else {
                return Set.of(e1, e2);
            }
        }
        return Set.of();
    }

    public boolean contains(ChatEntity chat, TauMemberEntity member) throws DatabaseException {
        if (chat instanceof GroupEntity group) {
            return groupRepo.contains(group, member);
        }

        if (chat instanceof DialogEntity dialog) {
            if (dialog.firstMember().equals(member)) return true;
            return dialog.secondMember().equals(member);
        }
        return false;
    }
}
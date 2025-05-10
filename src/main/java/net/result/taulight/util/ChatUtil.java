package net.result.taulight.util;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.taulight.db.*;

import java.util.*;

public class ChatUtil {
    private final ChannelRepository channelRepo;
    private final DialogRepository dialogRepo;

    public ChatUtil(Container container) {
        super();
        channelRepo = container.get(ChannelRepository.class);
        dialogRepo = container.get(DialogRepository.class);
    }

    public Optional<ChatEntity> getChat(UUID id) throws DatabaseException {
        Optional<ChannelEntity> channel = channelRepo.findById(id);
        if (channel.isPresent()) return channel.map(c -> c);
        return dialogRepo.findById(id).map(d -> d);
    }

    public Collection<TauMemberEntity> getMembers(ChatEntity chat) {
        if (chat instanceof ChannelEntity channel) return channel.members();
        if (chat instanceof DialogEntity dialog) {
            TauMemberEntity e1 = dialog.firstMember();
            TauMemberEntity e2 = dialog.secondMember();
            if (e1 == e2) {
                return Set.of(e1);
            } else {
                return Set.of(e1, e2);
            }
        }
        return Set.of();
    }
}
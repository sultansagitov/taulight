package net.result.taulight.db;

import net.result.sandnode.exception.DatabaseException;

import java.util.*;

public class TauJPADatabase implements TauDatabase {
    private final ChannelRepository channelRepo;
    private final DialogRepository dialogRepo;

    public TauJPADatabase() {
        super();
        channelRepo = new ChannelRepository();
        dialogRepo = new DialogRepository();
    }

    @Override
    public Optional<ChatEntity> getChat(UUID id) throws DatabaseException {
        Optional<ChannelEntity> channel = channelRepo.findById(id);
        if (channel.isPresent()) return channel.map(c -> c);
        return dialogRepo.findById(id).map(d -> d);
    }

    @Override
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
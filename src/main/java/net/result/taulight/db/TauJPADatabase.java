package net.result.taulight.db;

import net.result.sandnode.db.JPADatabase;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.security.PasswordHasher;

import java.util.*;

public class TauJPADatabase extends JPADatabase implements TauDatabase {
    private final ChannelRepository channelRepo;
    private final DialogRepository dialogRepo;
    private final RoleRepository roleRepo;

    public TauJPADatabase(PasswordHasher hasher) {
        super(hasher);
        channelRepo = new ChannelRepository();
        dialogRepo = new DialogRepository();
        roleRepo = new RoleRepository();
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

    @Override
    public RoleEntity createRole(ChannelEntity channel, String role) throws DatabaseException {
        return roleRepo.create(channel, role);
    }

    @Override
    public boolean addMemberToRole(RoleEntity role, TauMemberEntity member) throws DatabaseException {
        return roleRepo.addMember(role, member);
    }

}
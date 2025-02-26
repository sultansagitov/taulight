package net.result.taulight.db;

import net.result.sandnode.db.Member;
import net.result.sandnode.exception.DatabaseException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class TauChat {
    private final UUID id;
    private final TauDatabase database;

    public TauChat(UUID id, TauDatabase database) {
        this.id = id;
        this.database = database;
    }

    public UUID id() {
        return id;
    }

    public Collection<Member> getMembers() throws DatabaseException {
        return database.getMembersFromChat(this);
    }

    public void addMember(Member member) throws DatabaseException {
        database.addMemberToChat(this, member);
    }

    public List<ServerChatMessage> loadMessages(int index, int size) throws DatabaseException {
        return database.loadMessages(this, index, size);
    }

    public long getMessageCount() throws DatabaseException {
        return database.getMessageCount(this);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof TauChat chat && id.equals(chat.id);
    }
}

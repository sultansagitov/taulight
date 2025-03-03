package net.result.taulight.db;

import net.result.sandnode.db.Member;
import net.result.sandnode.db.SandnodeObject;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.message.ChatInfo;
import net.result.taulight.message.ChatInfoProp;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class TauChat extends SandnodeObject {
    private final TauDatabase database;

    public TauChat(UUID id, TauDatabase database) {
        setID(id);
        this.database = database;
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

    abstract public boolean hasMatchingProps(Collection<ChatInfoProp> chatInfoProps);

    abstract public ChatInfo getInfo(Member member, Collection<ChatInfoProp> chatInfoProps);

    @Override
    public int hashCode() {
        return id().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof TauChat chat && id().equals(chat.id());
    }
}

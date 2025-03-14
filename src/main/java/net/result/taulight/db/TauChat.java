package net.result.taulight.db;

import net.result.sandnode.db.Member;
import net.result.sandnode.db.SandnodeObject;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.message.ChatInfo;
import net.result.taulight.message.ChatInfoProp;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class TauChat extends SandnodeObject {
    private final TauDatabase database;

    public TauChat(TauDatabase database) {
        super();
        this.database = database;
    }

    public TauChat(UUID id, ZonedDateTime creationDate, TauDatabase database) {
        super(id, creationDate);
        this.database = database;
    }

    public TauDatabase database() {
        return database;
    }

    public abstract Collection<Member> getMembers() throws DatabaseException;

    public List<ServerChatMessage> loadMessages(int index, int size) throws DatabaseException {
        return database().loadMessages(this, index, size);
    }

    public long getMessageCount() throws DatabaseException {
        return database().getMessageCount(this);
    }

    abstract public boolean hasMatchingProps(Collection<ChatInfoProp> chatInfoProps);

    abstract public ChatInfo getInfo(Member member, Collection<ChatInfoProp> chatInfoProps);

}

package net.result.taulight.db;

import net.result.sandnode.db.Member;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.exception.AlreadyExistingRecordException;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.dto.ChatInfo;
import net.result.taulight.dto.ChatInfoProp;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class TauChat extends TaulightObject {
    public TauChat(TauDatabase database) {
        super(database);
    }

    public TauChat(UUID id, ZonedDateTime creationDate, TauDatabase database) {
        super(database, id, creationDate);
    }

    public void save() throws AlreadyExistingRecordException, DatabaseException {
        database().saveChat(this);
    }

    public abstract Collection<Member> getMembers() throws DatabaseException;

    public List<ChatMessageViewDTO> loadMessages(int index, int size) throws DatabaseException {
        return database().loadMessages(this, index, size);
    }

    public long getMessageCount() throws DatabaseException {
        return database().getMessageCount(this);
    }

    abstract public boolean hasMatchingProps(Collection<ChatInfoProp> chatInfoProps);

    abstract public ChatInfo getInfo(Member member, Collection<ChatInfoProp> chatInfoProps);

}

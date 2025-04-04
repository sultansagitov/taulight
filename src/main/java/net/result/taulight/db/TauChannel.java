package net.result.taulight.db;

import net.result.sandnode.db.Member;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.dto.ChatInfo;
import net.result.taulight.dto.ChatInfoProp;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class TauChannel extends TauChat {
    private final String title;
    private final Member owner;

    public TauChannel(UUID id, ZonedDateTime creationDate, TauDatabase database, String title, Member owner) {
        super(id, creationDate, database);
        this.title = title;
        this.owner = owner;
    }

    public TauChannel(TauDatabase database, String title, Member owner) {
        super(database);
        this.title = title;
        this.owner = owner;
    }

    @Override
    public Collection<Member> getMembers() throws DatabaseException {
        return database().getMembersFromChannel(this);
    }

    public void addMember(Member member) throws DatabaseException {
        database().addMemberToChat(this, member);
    }

    public String title() {
        return title;
    }

    public Member owner() {
        return owner;
    }

    @Override
    public boolean hasMatchingProps(Collection<ChatInfoProp> chatInfoProps) {
        return !Collections.disjoint(chatInfoProps, ChatInfoProp.channelAll());
    }

    @Override
    public ChatInfo getInfo(Member member, Collection<ChatInfoProp> chatInfoProps) {
        return ChatInfo.channel(this, member, chatInfoProps);
    }

    public Collection<InviteCodeObject> getActiveInviteCodes() throws DatabaseException {
        return database().getActiveInviteCodes(this);
    }
}

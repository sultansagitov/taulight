package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.SandnodeEntity;
import net.result.sandnode.db.ZonedDateTimeConverter;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;

@Entity
public class MessageEntity extends SandnodeEntity {
    private String content;
    private boolean sys;

    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime sentDatetime;

    @ManyToOne(cascade = CascadeType.ALL)
    private ChatEntity chat;

    @ManyToOne(cascade = CascadeType.ALL)
    private MemberEntity member;

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<ReactionEntryEntity> reactionEntries = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<MessageEntity> replies = new HashSet<>();

    public ChatEntity chat() {
        return chat;
    }

    public void setChat(ChatEntity chat) {
        this.chat = chat;
    }

    public String content() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ZonedDateTime sentDatetime() {
        return sentDatetime;
    }

    public void setSentDatetime(ZonedDateTime sentDatetime) {
        this.sentDatetime = sentDatetime;
    }

    public MemberEntity member() {
        return member;
    }

    public void setMember(MemberEntity member) {
        this.member = member;
    }

    public boolean sys() {
        return sys;
    }

    public void setSys(boolean sys) {
        this.sys = sys;
    }

    public Collection<ReactionEntryEntity> reactionEntries() {
        return reactionEntries;
    }

    public void setReactionEntries(Collection<ReactionEntryEntity> reactionEntries) {
        this.reactionEntries = reactionEntries;
    }

    public Collection<MessageEntity> replies() {
        return replies;
    }

    public void setReplies(Collection<MessageEntity> replies) {
        this.replies = replies;
    }
}

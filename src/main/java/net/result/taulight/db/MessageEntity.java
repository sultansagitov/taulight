package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.SandnodeEntity;
import net.result.sandnode.db.ZonedDateTimeConverter;
import net.result.taulight.dto.ChatMessageInputDTO;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.*;

@SuppressWarnings("unused")
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
    private Set<ReactionEntryEntity> reactionEntries = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Set<MessageEntity> replies = new HashSet<>();

    public MessageEntity() {
        super();
    }

    public MessageEntity(ChatEntity chat, ChatMessageInputDTO input, MemberEntity member) {
        super();
        setChat(chat);
        setSentDatetime(input.sentDatetime());
        setContent(input.content());
        setMember(member);
        setSys(input.sys());
    }

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

    public Set<ReactionEntryEntity> reactionEntries() {
        return reactionEntries;
    }

    public void setReactionEntries(Set<ReactionEntryEntity> reactionEntries) {
        this.reactionEntries = reactionEntries;
    }

    public Set<MessageEntity> replies() {
        return replies;
    }

    public void setReplies(Set<MessageEntity> replies) {
        this.replies = replies;
    }
}

package net.result.taulight.db;

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

    @ManyToOne
    private ChatEntity chat;

    @ManyToOne
    private TauMemberEntity member;

    @OneToMany(mappedBy = "message", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<ReactionEntryEntity> reactionEntries = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "message_replies",
            joinColumns = @JoinColumn(name = "reply_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "original_id", nullable = false)
    )
    private Set<MessageEntity> repliedToMessages = new HashSet<>();

    @ManyToMany(mappedBy = "repliedToMessages")
    private Set<MessageEntity> replies = new HashSet<>();

    public MessageEntity() {}

    public MessageEntity(ChatEntity chat, ChatMessageInputDTO input, TauMemberEntity member) {
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

    public TauMemberEntity member() {
        return member;
    }

    public void setMember(TauMemberEntity member) {
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

    public Set<MessageEntity> repliedToMessages() {
        return repliedToMessages;
    }

    public void setRepliedToMessages(Set<MessageEntity> repliedToMessages) {
        this.repliedToMessages = repliedToMessages;
    }

    public Set<MessageEntity> replies() {
        return replies;
    }

    public void setReplies(Set<MessageEntity> replies) {
        this.replies = replies;
    }

    @Override
    public String toString() {
        return "<MessageEntity content='%s' sys=%s chat=%s member=%s repliedToMessages=%s>"
                .formatted(content, sys, chat, member, repliedToMessages);

    }
}

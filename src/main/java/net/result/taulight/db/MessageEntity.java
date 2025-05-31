package net.result.taulight.db;

import jakarta.persistence.*;
import net.result.sandnode.db.BaseEntity;
import net.result.sandnode.db.EncryptedKeyEntity;
import net.result.sandnode.db.ZonedDateTimeConverter;
import net.result.taulight.dto.ChatMessageInputDTO;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
public class MessageEntity extends BaseEntity {
    private boolean sys;

    @Lob
    private String content;

    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime sentDatetime;

    @ManyToOne
    private EncryptedKeyEntity key;

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

    @ManyToMany(mappedBy = "repliedToMessages", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<MessageEntity> replies = new HashSet<>();

    public MessageEntity() {}

    public MessageEntity(
            ChatEntity chat,
            ChatMessageInputDTO input,
            TauMemberEntity member,
            @Nullable EncryptedKeyEntity key
    ) {
        setChat(chat);
        setMember(member);
        if (key != null) setKey(key);

        setSentDatetime(input.sentDatetime);
        setContent(input.content);
        setSys(input.sys);
    }

    public EncryptedKeyEntity key() {
        return key;
    }

    public void setKey(EncryptedKeyEntity key) {
        this.key = key;
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

package net.result.taulight.entity;

import jakarta.persistence.*;
import net.result.sandnode.db.ZonedDateTimeConverter;
import net.result.sandnode.entity.BaseEntity;
import net.result.sandnode.entity.EncryptedKeyEntity;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.dto.NamedFileDTO;
import net.result.taulight.repository.MessageFileRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    public @NotNull ChatMessageInputDTO toInputDTO(Set<UUID> fileIDs) {
        return new ChatMessageInputDTO(
                chat().id(),
                key() != null ? key().id() : null,
                content(),
                sentDatetime(),
                member().member().nickname(),
                sys(),
                repliedToMessages().stream().map(BaseEntity::id).collect(Collectors.toSet()),
                fileIDs
        );
    }

    public @NotNull ChatMessageViewDTO toViewDTO(MessageFileRepository messageFileRepo) {
        Map<String, List<String>> reactions = new HashMap<>();
        reactionEntries().forEach((entry) -> {
            ReactionTypeEntity type = entry.reactionType();
            TauMemberEntity member = entry.member();

            String reaction = "%s:%s".formatted(type.reactionPackage().name(), type.name());

            reactions.computeIfAbsent(reaction, k -> new ArrayList<>()).add(member.member().nickname());
        });

        Collection<MessageFileEntity> files = messageFileRepo.getFiles(this);

        List<NamedFileDTO> namedFiles = new ArrayList<>(files.size());
        Set<UUID> fileIDs = new HashSet<>(files.size());

        for (MessageFileEntity msgFile : files) {
            namedFiles.add(msgFile.toDTO());
            fileIDs.add(msgFile.id());
        }

        ChatMessageInputDTO input = toInputDTO(fileIDs);

        return new ChatMessageViewDTO(input, id(), creationDate(), reactions, namedFiles);
    }
}

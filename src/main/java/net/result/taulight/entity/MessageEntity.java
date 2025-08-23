package net.result.taulight.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

@Setter
@Getter
@NoArgsConstructor
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

    @Override
    public String toString() {
        return "<MessageEntity content='%s' sys=%s chat=%s member=%s repliedToMessages=%s>"
                .formatted(content, sys, chat, member, repliedToMessages);
    }

    public @NotNull ChatMessageInputDTO toInputDTO(Set<UUID> fileIDs) {
        return new ChatMessageInputDTO(
                getChat().id(),
                getKey() != null ? getKey().id() : null,
                getContent(),
                getSentDatetime(),
                getMember().getMember().getNickname(),
                isSys(),
                getRepliedToMessages().stream().map(BaseEntity::id).collect(Collectors.toSet()),
                fileIDs
        );
    }

    public @NotNull ChatMessageViewDTO toViewDTO(MessageFileRepository messageFileRepo) {
        Map<String, List<String>> reactions = new HashMap<>();
        getReactionEntries().forEach((entry) -> {
            ReactionTypeEntity type = entry.getReactionType();
            TauMemberEntity member = entry.getMember();

            String reaction = "%s:%s".formatted(type.getReactionPackage().getName(), type.getName());

            reactions.computeIfAbsent(reaction, k -> new ArrayList<>()).add(member.getMember().getNickname());
        });

        Collection<MessageFileEntity> files = messageFileRepo.getFiles(this);

        List<NamedFileDTO> namedFiles = new ArrayList<>(files.size());
        Set<UUID> fileIDs = new HashSet<>(files.size());

        for (MessageFileEntity msgFile : files) {
            namedFiles.add(msgFile.toDTO());
            fileIDs.add(msgFile.id());
        }

        ChatMessageInputDTO input = toInputDTO(fileIDs);

        return new ChatMessageViewDTO(input, id(), getCreationDate(), reactions, namedFiles);
    }
}

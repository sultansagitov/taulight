package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
public class ChannelEntity extends ChatEntity {
    private final String title;
    private final MemberEntity owner;

    @ManyToMany
    private List<MemberEntity> members;

    public ChannelEntity(String title, MemberEntity owner) {
        super();
        this.title = title;
        this.owner = owner;
    }

    public ChannelEntity(UUID id, ZonedDateTime creationDate, String title, MemberEntity owner) {
        super(id, creationDate);
        this.title = title;
        this.owner = owner;
    }

    public String title() {
        return title;
    }

    public MemberEntity owner() {
        return owner;
    }

    @Override
    public boolean hasMatchingProps(Collection<ChatInfoPropDTO> chatInfoProps) {
        return !Collections.disjoint(chatInfoProps, ChatInfoPropDTO.channelAll());
    }

    @Override
    public ChatInfoDTO getInfo(MemberEntity member, Collection<ChatInfoPropDTO> chatInfoProps) {
        return ChatInfoDTO.channel(this, member, chatInfoProps);
    }
}

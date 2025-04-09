package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.SandnodeEntity;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

public abstract class ChatEntity extends SandnodeEntity {
    public ChatEntity() {
        super();
    }

    public ChatEntity(UUID id, ZonedDateTime creationDate) {
        super(id, creationDate);
    }

    abstract public boolean hasMatchingProps(Collection<ChatInfoPropDTO> chatInfoProps);

    abstract public ChatInfoDTO getInfo(MemberEntity member, Collection<ChatInfoPropDTO> chatInfoProps);

}

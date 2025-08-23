package net.result.taulight.entity;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.result.sandnode.entity.BaseEntity;
import net.result.sandnode.db.ZonedDateTimeConverter;
import net.result.taulight.dto.InviteCodeDTO;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class InviteCodeEntity extends BaseEntity {
    private String code;

    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime activatedAt;

    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime expiresDate;

    @ManyToOne
    private GroupEntity group;

    @ManyToOne
    private TauMemberEntity receiver;

    @ManyToOne
    private TauMemberEntity sender;

    public InviteCodeEntity(
            GroupEntity group,
            TauMemberEntity receiver,
            TauMemberEntity sender,
            ZonedDateTime expiresDate
    ) {
        setRandomCode();
        this.group = group;
        this.receiver = receiver;
        this.sender = sender;
        this.expiresDate = expiresDate;
        this.activatedAt = null;
    }

    public void setRandomCode() {
        SecureRandom random = new SecureRandom();
        code = IntStream.range(0, 64)
                .mapToObj(i -> String.valueOf((char) ('a' + random.nextInt(26))))
                .collect(Collectors.joining());
    }

    public void setActivationDateNow() {
        this.activatedAt = ZonedDateTime.now();
    }

    public InviteCodeDTO toDTO() {
        return new InviteCodeDTO(
                getCode(),
                getGroup().getTitle(),
                getReceiver().getMember().getNickname(),
                getSender().getMember().getNickname(),
                getCreationDate(),
                getActivatedAt(),
                getExpiresDate()
        );
    }

    @Override
    public String toString() {
        return "<InviteCodeEntity code=%s, group=%s, receiver=%s, sender=%s>".formatted(
                code,
                group.getTitle(),
                receiver.getMember().getNickname(),
                sender.getMember().getNickname()
        );
    }
}

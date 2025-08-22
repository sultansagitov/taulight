package net.result.taulight.entity;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Setter;
import net.result.sandnode.entity.BaseEntity;
import net.result.sandnode.db.ZonedDateTimeConverter;
import net.result.taulight.dto.InviteCodeDTO;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
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

    @Setter
    @ManyToOne
    private TauMemberEntity sender;

    public InviteCodeEntity() {}

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

    public ZonedDateTime expiresDate() {
        return expiresDate;
    }

    public void setExpiresDate(ZonedDateTime expiresDate) {
        this.expiresDate = expiresDate;
    }

    public String code() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setRandomCode() {
        SecureRandom random = new SecureRandom();
        code = IntStream.range(0, 64)
                .mapToObj(i -> String.valueOf((char) ('a' + random.nextInt(26))))
                .collect(Collectors.joining());
    }

    public GroupEntity group() {
        return group;
    }

    public void setGroup(GroupEntity group) {
        this.group = group;
    }

    public TauMemberEntity receiver() {
        return receiver;
    }

    public void setReceiver(TauMemberEntity receiver) {
        this.receiver = receiver;
    }

    public TauMemberEntity sender() {
        return sender;
    }

    public ZonedDateTime activationDate() {
        return activatedAt;
    }

    public void setActivationDate(ZonedDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public void setActivationDateNow() {
        this.activatedAt = ZonedDateTime.now();
    }

    public InviteCodeDTO toDTO() {
        return new InviteCodeDTO(
                code(),
                group().title(),
                receiver().getMember().getNickname(),
                sender().getMember().getNickname(),
                creationDate(),
                activationDate(),
                expiresDate()
        );
    }

    @Override
    public String toString() {
        return "<InviteCodeEntity code=%s, group=%s, receiver=%s, sender=%s>".formatted(
                code,
                group.title(),
                receiver.getMember().getNickname(),
                sender.getMember().getNickname()
        );
    }
}

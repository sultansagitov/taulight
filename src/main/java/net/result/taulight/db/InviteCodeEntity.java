package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.SandnodeEntity;
import net.result.sandnode.db.ZonedDateTimeConverter;

import javax.persistence.CascadeType;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
@Entity
public class InviteCodeEntity extends SandnodeEntity {
    private String code;
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime activatedAt;
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime expiresDate;

    @ManyToOne(cascade = CascadeType.ALL)
    private ChannelEntity channel;

    @ManyToOne(cascade = CascadeType.ALL)
    private MemberEntity receiver;

    @ManyToOne(cascade = CascadeType.ALL)
    private MemberEntity sender;

    public InviteCodeEntity() {
        super();
    }

    public InviteCodeEntity(
            ChannelEntity channel,
            MemberEntity receiver,
            MemberEntity sender,
            ZonedDateTime expiresDate
    ) {
        super();
        setRandomCode();
        this.channel = channel;
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

    public ChannelEntity channel() {
        return channel;
    }

    public void setChannel(ChannelEntity channel) {
        this.channel = channel;
    }

    public MemberEntity receiver() {
        return receiver;
    }

    public void setReceiver(MemberEntity receiver) {
        this.receiver = receiver;
    }

    public MemberEntity sender() {
        return sender;
    }

    public void setSender(MemberEntity sender) {
        this.sender = sender;
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
}

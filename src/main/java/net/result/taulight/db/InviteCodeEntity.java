package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.SandnodeEntity;
import net.result.sandnode.db.ZonedDateTimeConverter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Entity
public class InviteCodeEntity extends SandnodeEntity {
    private String code;
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime activatedAt;
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime expiresDate;

    @ManyToOne
    private ChatEntity chat;
    @ManyToOne
    private MemberEntity receiver;
    @ManyToOne
    private MemberEntity sender;

    @SuppressWarnings("unused")
    public InviteCodeEntity() {
        super();
    }

    public InviteCodeEntity(ChatEntity chat, MemberEntity receiver, MemberEntity sender, ZonedDateTime expiresDate) {
        super();
        setRandomCode();
        this.chat = chat;
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

    public ZonedDateTime expiresData() {
        return expiresDate;
    }

    public String code() {
        return code;
    }

    public ChatEntity chat() {
        return chat;
    }

    public MemberEntity receiver() {
        return receiver;
    }

    public MemberEntity sender() {
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
}

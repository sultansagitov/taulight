package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.Member;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.exception.AlreadyExistingRecordException;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InviteCodeObject extends TaulightObject {
    @JsonProperty("expires-at")
    private ZonedDateTime expiresDate;
    @JsonProperty
    private String nickname;
    @JsonProperty("chat-id")
    private UUID chatID;
    @JsonProperty
    private String code;
    @JsonProperty("sender-nickname")
    private String senderNickname;
    @JsonProperty("activated-at")
    private @Nullable ZonedDateTime activatedAt;

    @SuppressWarnings("unused")
    public InviteCodeObject() {
        super();
    }

    public InviteCodeObject(TauDatabase database,
                            TauChat chat, Member member, Member sender, ZonedDateTime expiresDate) {
        super(database);
        setRandomCode();
        this.chatID = chat.id();
        this.nickname = member.nickname();
        this.senderNickname = sender.nickname();
        this.expiresDate = expiresDate;
        this.activatedAt = null;
    }

    public InviteCodeObject(TauDatabase database, UUID id, ZonedDateTime createdAt,
                            String code, UUID chatID, String nickname, String senderNickname,
                            ZonedDateTime expiresDate, @Nullable ZonedDateTime activatedAt) {
        super(database, id, createdAt);
        this.expiresDate = expiresDate;
        this.nickname = nickname;
        this.chatID = chatID;
        this.code = code;
        this.senderNickname = senderNickname;
        this.activatedAt = activatedAt;
    }

    public void setRandomCode() {
        SecureRandom random = new SecureRandom();
        code = IntStream.range(0, 64)
                .mapToObj(i -> String.valueOf((char) ('a' + random.nextInt(26))))
                .collect(Collectors.joining());
    }

    public void save() throws DatabaseException {
        while (true) {
            try {
                database().createInviteCode(this);
                return;
            } catch (AlreadyExistingRecordException ignored) {
                setRandomCode();
            }
        }
    }

    public boolean activate() throws DatabaseException {
        return database().activateInviteCode(this);
    }

    public ZonedDateTime getExpiresData() {
        return expiresDate;
    }

    public String getNickname() {
        return nickname;
    }

    public UUID getChatID() {
        return chatID;
    }

    public String getCode() {
        return code;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public ZonedDateTime getActivationDate() {
        return activatedAt;
    }
}

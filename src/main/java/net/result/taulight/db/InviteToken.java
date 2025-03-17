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

public class InviteToken extends TaulightObject {
    @JsonProperty("expires-at")
    private ZonedDateTime expiresDate;
    @JsonProperty
    private String nickname;
    @JsonProperty("chat-id")
    private UUID chatID;
    @JsonProperty("reject-code")
    private String rejectCode;
    @JsonProperty("sender-nickname")
    private String senderNickname;
    @JsonProperty("activated-at")
    private @Nullable ZonedDateTime activatedAt;

    @SuppressWarnings("unused")
    public InviteToken() {
        super();
    }

    public InviteToken(TauDatabase database, TauChat chat, Member member, Member sender, ZonedDateTime expiresDate) {
        super(database);
        setRandomRejectCode();
        this.chatID = chat.id();
        this.nickname = member.nickname();
        this.senderNickname = sender.nickname();
        this.expiresDate = expiresDate;
        this.activatedAt = null;
    }

    public InviteToken(TauDatabase database, UUID id, ZonedDateTime createdAt,
                       String rejectCode, UUID chatID, String nickname, String senderNickname,
                       ZonedDateTime expiresDate, @Nullable ZonedDateTime activatedAt) {
        super(database, id, createdAt);
        this.expiresDate = expiresDate;
        this.nickname = nickname;
        this.chatID = chatID;
        this.rejectCode = rejectCode;
        this.senderNickname = senderNickname;
        this.activatedAt = activatedAt;
    }

    public void setRandomRejectCode() {
        SecureRandom random = new SecureRandom();
        rejectCode = IntStream.range(0, 64)
                .mapToObj(i -> String.valueOf((char) ('a' + random.nextInt(26))))
                .collect(Collectors.joining());
    }

    public void save() throws DatabaseException {
        while (true) {
            try {
                database().createInviteToken(this);
                return;
            } catch (AlreadyExistingRecordException ignored) {
                setRandomRejectCode();
            }
        }
    }

    public boolean activate() throws DatabaseException {
        return database().activateInviteToken(this);
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

    public String getRejectCode() {
        return rejectCode;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public ZonedDateTime getActivationDate() {
        return activatedAt;
    }
}

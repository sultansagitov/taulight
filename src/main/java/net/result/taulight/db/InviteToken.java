package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.exception.AlreadyExistingRecordException;

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

    @SuppressWarnings("unused")
    public InviteToken() {
        super();
    }

    public InviteToken(TauDatabase database, ZonedDateTime expiresDate, String nickname, UUID chatID) {
        super(database);
        this.expiresDate = expiresDate;
        this.nickname = nickname;
        this.chatID = chatID;
        setRandomRejectCode();
    }

    public InviteToken(TauDatabase database, UUID id, ZonedDateTime createdAt,
                       ZonedDateTime expiresDate, String nickname, UUID chatID, String rejectCode) {
        super(database, id, createdAt);
        this.expiresDate = expiresDate;
        this.nickname = nickname;
        this.chatID = chatID;
        this.rejectCode = rejectCode;
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
                setRandomID();
                setRandomRejectCode();
                database().createInviteToken(this);
                return;
            } catch (AlreadyExistingRecordException ignored) {
            }
        }
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
}

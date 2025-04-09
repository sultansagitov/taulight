package net.result.sandnode.db;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Member extends SandnodeEntity {
    private String nickname;
    private String passwordHash;

    public Member(String nickname, String passwordHash) {
        super();
        this.setNickname(nickname);
        this.setPasswordHash(passwordHash);
    }

    public Member(UUID id, ZonedDateTime creationDate, String nickname, String passwordHash) {
        super(id, creationDate);
        this.setNickname(nickname);
        this.setPasswordHash(passwordHash);
    }

    public String nickname() {
        return nickname;
    }

    public String hashedPassword() {
        return passwordHash;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return "<Member %s %s>".formatted(id(), nickname());
    }
}

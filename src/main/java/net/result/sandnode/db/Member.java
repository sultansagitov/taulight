package net.result.sandnode.db;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Member extends SandnodeObject {
    private final String nickname;
    private final String password;

    public Member(String nickname, String password) {
        setRandomID();
        setCreationDateNow();
        this.nickname = nickname;
        this.password = password;
    }

    public Member(UUID id, ZonedDateTime createdAt, String nickname, String passwordHash) {
        setID(id);
        setCreationDate(createdAt);
        this.nickname = nickname;
        this.password = passwordHash;
    }

    public String nickname() {
        return nickname;
    }

    public String hashedPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "<Member %s %s>".formatted(id(), nickname());
    }

}

package net.result.sandnode.db;

import java.time.ZonedDateTime;
import java.util.UUID;

public class StandardMember extends Member {
    private final String nickname;
    private final String password;

    public StandardMember(String nickname, String password) {
        setRandomID();
        setCreationDateNow();
        this.nickname = nickname;
        this.password = password;
    }

    public StandardMember(UUID id, ZonedDateTime createdAt, String nickname, String passwordHash) {
        setID(id);
        setCreationDate(createdAt);
        this.nickname = nickname;
        this.password = passwordHash;
    }

    @Override
    public String nickname() {
        return nickname;
    }

    @Override
    public String hashedPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "<Member %s>".formatted(nickname());
    }

    @Override
    public int hashCode() {
        return nickname.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StandardMember that = (StandardMember) obj;
        return nickname.equals(that.nickname);
    }
}

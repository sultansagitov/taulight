package net.result.sandnode.db;

import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHasher;

import java.util.*;

public class InMemoryDatabase implements Database {
    public final Collection<Member> db = new HashSet<>();
    private final PasswordHasher hasher;

    public InMemoryDatabase(PasswordHasher hasher) {
        this.hasher = hasher;
    }

    @Override
    public synchronized Member registerMember(String nickname, String password) throws BusyNicknameException {
        if (db.stream().anyMatch(member -> member.nickname().equals(nickname))) {
            throw new BusyNicknameException();
        }

        String hashedPassword = hasher().hash(password, 12);
        Member member = new Member(this, nickname, hashedPassword);
        db.add(member);
        return member;
    }

    @Override
    public synchronized Optional<Member> findMemberByNickname(String nickname) {
        return db.stream().filter(m -> m.nickname().equals(nickname)).findFirst();
    }

    @Override
    public PasswordHasher hasher() {
        return hasher;
    }
}

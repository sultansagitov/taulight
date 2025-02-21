package net.result.sandnode.db;

import net.result.sandnode.exception.BusyMemberIDException;
import net.result.sandnode.security.PasswordHasher;

import java.util.*;

public class InMemoryDatabase implements Database {
    public final Collection<Member> db = new HashSet<>();
    private final PasswordHasher hasher;

    public InMemoryDatabase(PasswordHasher hasher) {
        this.hasher = hasher;
    }

    @Override
    public synchronized Member registerMember(String memberID, String password) throws BusyMemberIDException {
        if (db.stream().anyMatch(member -> member.id().equals(memberID))) {
            throw new BusyMemberIDException();
        }

        Member member = new StandardMember(memberID, password);
        db.add(member);
        return member;
    }

    @Override
    public synchronized Optional<Member> findMemberByMemberID(String memberID) {
        return db.stream().filter(m -> m.id().equals(memberID)).findFirst();
    }

    @Override
    public PasswordHasher hasher() {
        return hasher;
    }
}

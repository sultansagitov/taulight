package net.result.sandnode.db;

import net.result.sandnode.exception.BusyMemberIDException;

import java.util.*;

public class InMemoryDatabase implements Database {
    public final Collection<Member> db = new HashSet<>();

    @Override
    public synchronized Member registerMember(String memberID, String password) throws BusyMemberIDException {
        if (db.stream().anyMatch(member -> member.getID().equals(memberID))) {
            throw new BusyMemberIDException(memberID);
        }

        Member member = new StandardMember(memberID, password);
        db.add(member);
        return member;
    }

    @Override
    public synchronized Optional<Member> findMemberByMemberID(String memberID) {
        return db.stream().filter(m -> m.getID().equals(memberID)).findFirst();
    }
}

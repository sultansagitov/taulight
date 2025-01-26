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

        Member member = new StandardMember(memberID, password, this);
        db.add(member);
        return member;
    }

    @Override
    public synchronized Optional<Member> findMemberByMemberID(String agentID) {
        for (Member member : db) {
            if (member.getID().equals(agentID)) {
                return Optional.of(member);
            }
        }

        return Optional.empty();
    }
}

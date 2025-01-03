package net.result.sandnode.util.db;

import net.result.sandnode.exceptions.BusyMemberIDException;

import java.util.*;

public class InMemoryDatabase implements IDatabase {
    public final Set<IMember> db = new HashSet<>();

    @Override
    public synchronized IMember registerMember(String memberID, String password) throws BusyMemberIDException {
        if (db.stream().anyMatch(member -> member.getID().equals(memberID))) {
            throw new BusyMemberIDException(memberID);
        }

        IMember member = new Member(memberID, password, this);
        db.add(member);
        return member;
    }

    @Override
    public synchronized Optional<IMember> findMemberByMemberID(String agentID) {
        for (IMember member : db) {
            if (member.getID().equals(agentID)) {
                return Optional.of(member);
            }
        }

        return Optional.empty();
    }
}

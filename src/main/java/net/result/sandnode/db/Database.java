package net.result.sandnode.db;

import net.result.sandnode.exception.BusyMemberIDException;

import java.util.Optional;

public interface Database {

    Member registerMember(String memberID, String password) throws BusyMemberIDException;

    Optional<Member> findMemberByMemberID(String memberID);

}

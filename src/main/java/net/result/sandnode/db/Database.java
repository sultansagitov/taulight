package net.result.sandnode.db;

import net.result.sandnode.exception.BusyMemberIDException;
import net.result.sandnode.exception.DatabaseException;

import java.util.Optional;

public interface Database {

    Member registerMember(String memberID, String password) throws BusyMemberIDException, DatabaseException;

    Optional<Member> findMemberByMemberID(String memberID) throws DatabaseException;

}

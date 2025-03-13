package net.result.sandnode.db;

import net.result.sandnode.exception.error.BusyMemberIDException;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.security.PasswordHasher;

import java.util.Optional;

public interface Database {

    Member registerMember(String nickname, String password) throws BusyMemberIDException, DatabaseException;

    Optional<Member> findMemberByNickname(String nickname) throws DatabaseException;

    PasswordHasher hasher();

}

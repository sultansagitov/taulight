package net.result.sandnode.db;

import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.security.PasswordHasher;

import java.util.Optional;

public interface Database {

    MemberEntity registerMember(String nickname, String password) throws BusyNicknameException, DatabaseException;

    Optional<MemberEntity> findMemberByNickname(String nickname) throws DatabaseException;

    PasswordHasher hasher();

}

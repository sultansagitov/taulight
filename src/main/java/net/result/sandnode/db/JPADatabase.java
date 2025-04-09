package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHasher;

import java.util.Optional;

public class JPADatabase implements Database {
    @Override
    public Member registerMember(String nickname, String password) throws BusyNicknameException, DatabaseException {
        return null;
    }

    @Override
    public Optional<Member> findMemberByNickname(String nickname) throws DatabaseException {
        return Optional.empty();
    }

    @Override
    public PasswordHasher hasher() {
        return null;
    }
}

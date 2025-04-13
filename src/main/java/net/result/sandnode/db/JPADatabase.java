package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHasher;

import java.util.Optional;

public class JPADatabase implements Database {
    private final PasswordHasher hasher;
    protected final MemberRepository memberRepo;

    public JPADatabase(PasswordHasher hasher) {
        this.hasher = hasher;
        memberRepo = new MemberRepository();
    }

    @Override
    public MemberEntity registerMember(String nickname, String password)
            throws BusyNicknameException, DatabaseException {
        if (findMemberByNickname(nickname).isPresent()) throw new BusyNicknameException();
        String hashedPassword = hasher.hash(password, 12);
        return memberRepo.save(new MemberEntity(nickname, hashedPassword));
    }

    @Override
    public Optional<MemberEntity> findMemberByNickname(String nickname) throws DatabaseException {
        return memberRepo.findByNickname(nickname);
    }

    @Override
    public PasswordHasher hasher() {
        return hasher;
    }
}

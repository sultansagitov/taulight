package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHasher;
import net.result.taulight.db.TauMemberEntity;
import net.result.taulight.db.TauMemberRepository;

import java.util.Optional;

public class JPADatabase implements Database {
    private final PasswordHasher hasher;
    protected final MemberRepository memberRepo;
    private final TauMemberRepository tauMemberRepo;

    public JPADatabase(PasswordHasher hasher) {
        this.hasher = hasher;
        memberRepo = new MemberRepository();
        tauMemberRepo = new TauMemberRepository();
    }

    @Override
    public MemberEntity registerMember(String nickname, String password)
            throws BusyNicknameException, DatabaseException {
        if (findMemberByNickname(nickname).isPresent()) throw new BusyNicknameException();
        String hashedPassword = hasher.hash(password, 12);
        MemberEntity member = memberRepo.create(nickname, hashedPassword);
        TauMemberEntity tauMember = tauMemberRepo.create(member);
        member.setTauMember(tauMember);
        return member;
    }

    @Override
    public Optional<MemberEntity> findMemberByNickname(String nickname) throws DatabaseException {
        return memberRepo.findByNickname(nickname);
    }

    @Override
    public boolean deleteMember(MemberEntity member) throws DatabaseException {
        return memberRepo.delete(member);
    }

    @Override
    public PasswordHasher hasher() {
        return hasher;
    }

    @Override
    public void shutdown() {
        JPAUtil.shutdown();
    }
}

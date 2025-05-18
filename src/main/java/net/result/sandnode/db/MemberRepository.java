package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.db.TauMemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Optional;

public class MemberRepository {
    private final JPAUtil jpaUtil;
    private final TauMemberRepository tauMemberRepo;

    public MemberRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
        tauMemberRepo = container.get(TauMemberRepository.class);
    }

    private MemberEntity save(MemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        while (em.find(MemberEntity.class, member.id()) != null) {
            member.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            MemberEntity merge = em.merge(member);
            transaction.commit();

            tauMemberRepo.create(merge);

            return merge;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to register member", e);
        }
    }

    public MemberEntity create(String nickname, String hashedPassword) throws DatabaseException, BusyNicknameException {
        if (findByNickname(nickname).isPresent()) throw new BusyNicknameException();
        return save(new MemberEntity(nickname, hashedPassword));
    }

    public Optional<MemberEntity> findByNickname(String nickname) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "FROM MemberEntity WHERE nickname = :nickname AND deleted = false";
            return em
                    .createQuery(q, MemberEntity.class)
                    .setParameter("nickname", nickname)
                    .setMaxResults(1)
                    .getResultList()
                    .stream().findFirst();
        } catch (Exception e) {
            throw new DatabaseException("Failed to find member by " + "nickname", e);
        }
    }

    public boolean delete(MemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            MemberEntity m = em.find(MemberEntity.class, member.id());
            if (m == null || m.deleted()) return false;

            transaction.begin();
            m.setDeleted(true);
            em.merge(m);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to delete invite code", e);
        }
    }

    public boolean setAvatar(MemberEntity member, FileEntity avatar) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            if (em.find(MemberEntity.class, member.id()) != null) {
                transaction.begin();
                member.setAvatar(avatar);
                em.merge(member);
                transaction.commit();
                return true;
            }

            return false;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to delete invite code", e);
        }
    }
}

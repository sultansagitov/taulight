package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Optional;

public class MemberRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    private MemberEntity save(MemberEntity member) throws DatabaseException {
        while (em.find(MemberEntity.class, member.id()) != null) {
            member.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            MemberEntity merge = em.merge(member);
            transaction.commit();
            return merge;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to register member", e);
        }
    }

    public MemberEntity create(String nickname, String hashedPassword) throws DatabaseException {
        return save(new MemberEntity(nickname, hashedPassword));
    }

    public Optional<MemberEntity> findByNickname(String nickname) throws DatabaseException {
        try {
            return em
                    .createQuery("FROM MemberEntity WHERE nickname = :nickname AND deleted = false", MemberEntity.class)
                    .setParameter("nickname", nickname)
                    .setMaxResults(1)
                    .getResultList()
                    .stream().findFirst();
        } catch (Exception e) {
            throw new DatabaseException("Failed to find member by " + "nickname", e);
        }
    }

    public boolean delete(MemberEntity member) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            if (em.find(MemberEntity.class, member.id()) != null) {
                transaction.begin();
                member.setDeleted(true);
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

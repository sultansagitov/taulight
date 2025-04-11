package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Optional;

public class MemberRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public void save(MemberEntity member) throws DatabaseException {
        while (em.find(MemberEntity.class, member.id()) != null) {
            member.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(member);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to register member", e);
        }
    }

    public Optional<MemberEntity> findByNickname(String nickname) throws DatabaseException {
        try {
            return em
                    .createQuery("FROM MemberEntity WHERE nickname = :nickname", MemberEntity.class)
                    .setParameter("nickname", nickname)
                    .setMaxResults(1)
                    .getResultList()
                    .stream().findFirst();
        } catch (Exception e) {
            throw new DatabaseException("Failed to find member by " + "nickname", e);
        }
    }
}

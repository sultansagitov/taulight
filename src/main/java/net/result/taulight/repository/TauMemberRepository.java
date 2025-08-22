package net.result.taulight.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import net.result.taulight.entity.TauMemberEntity;

import java.util.Optional;

public class TauMemberRepository {
    private final JPAUtil jpaUtil;

    public TauMemberRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public void create(MemberEntity member) {
        TauMemberEntity tauMember = new TauMemberEntity(member);

        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            em.merge(tauMember);

            MemberEntity m = tauMember.member();
            m.setTauMember(tauMember);

            em.merge(m);

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public Optional<TauMemberEntity> findByNickname(String nickname) {
        EntityManager em = jpaUtil.getEntityManager();
        String q = """
            FROM TauMemberEntity
            WHERE member.nickname = :nickname
        """;
        TypedQuery<TauMemberEntity> query = em.createQuery(q, TauMemberEntity.class)
                .setParameter("nickname", nickname)
                .setMaxResults(1);
        try {
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void setShowStatus(TauMemberEntity entity, boolean value) {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            entity.setShowStatus(value);
            em.merge(entity);

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new DatabaseException(e);
        }
    }
}

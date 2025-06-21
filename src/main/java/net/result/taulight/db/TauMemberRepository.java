package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

import java.util.Optional;

public class TauMemberRepository {
    private final JPAUtil jpaUtil;

    public TauMemberRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private TauMemberEntity save(TauMemberEntity tauMember) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        while (em.find(TauMemberEntity.class, tauMember.id()) != null) {
            tauMember.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            TauMemberEntity managed = em.merge(tauMember);

            MemberEntity member = tauMember.member();
            member.setTauMember(tauMember);

            em.merge(member);

            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public TauMemberEntity create(MemberEntity member) throws DatabaseException {
        return save(new TauMemberEntity(member));
    }

    public Optional<TauMemberEntity> findByNickname(String nickname) throws DatabaseException {
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
    public void setShowStatus(TauMemberEntity entity, boolean value) throws DatabaseException {
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

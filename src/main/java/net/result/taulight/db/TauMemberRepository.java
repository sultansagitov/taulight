package net.result.taulight.db;

import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

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
            TauMemberEntity merge = em.merge(tauMember);
            MemberEntity member = tauMember.member();
            member.setTauMember(tauMember);
            em.merge(member);
            transaction.commit();
            return merge;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to register member", e);
        }
    }

    public TauMemberEntity create(MemberEntity member) throws DatabaseException {
        return save(new TauMemberEntity(member));
    }
}

package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class TauMemberRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    private TauMemberEntity save(TauMemberEntity tauMember) throws DatabaseException {
        while (em.find(TauMemberEntity.class, tauMember.id()) != null) {
            tauMember.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            TauMemberEntity merge = em.merge(tauMember);
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

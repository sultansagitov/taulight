package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

public class TauMemberSettingsRepository {
    private final JPAUtil jpaUtil;

    public TauMemberSettingsRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public void setShowStatus(TauMemberSettingsEntity entity, boolean value) throws DatabaseException {
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

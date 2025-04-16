package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class ReactionTypeRepository {
    private final EntityManager em = JPAUtil.getEntityManager();
    private final ReactionPackageRepository reactionPackageRepo = new ReactionPackageRepository();

    private ReactionTypeEntity save(ReactionTypeEntity reactionType) throws DatabaseException {
        while (em.find(ReactionTypeEntity.class, reactionType.id()) != null) {
            reactionType.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            reactionType.setRandomID();
            transaction.begin();
            ReactionTypeEntity managed = em.merge(reactionType);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to save reaction type", e);
        }
    }

    public ReactionTypeEntity create(String name, ReactionPackageEntity reactionPackage) throws DatabaseException {
        return save(new ReactionTypeEntity(name, reactionPackage));
    }

    public ReactionTypeEntity create(String name, String packageName) throws DatabaseException {
        Optional<ReactionPackageEntity> reactionPackage = reactionPackageRepo.find(packageName);
        if (reactionPackage.isPresent()) {
            return create(name, reactionPackage.get());
        }

        return create(name, reactionPackageRepo.create(packageName));
    }

    public List<ReactionTypeEntity> findByPackageName(String packageName) throws DatabaseException {
        try {
            String q = "FROM ReactionTypeEntity WHERE reactionPackage.name = :packageName";
            return em.createQuery(q, ReactionTypeEntity.class).setParameter("packageName", packageName).getResultList();
        } catch (Exception e) {
            throw new DatabaseException("Failed to find reaction types by package name", e);
        }
    }
}

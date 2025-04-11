package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.exception.AlreadyExistingRecordException;
import org.jetbrains.annotations.NotNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.Optional;
import java.util.UUID;

public class DialogRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public void save(@NotNull DialogEntity dialog) throws AlreadyExistingRecordException, DatabaseException {
        while (em.find(DialogEntity.class, dialog.id()) != null) {
            dialog.setRandomID();
        }

        Optional<DialogEntity> resultList = findByMembers(dialog.firstMember(), dialog.secondMember());

        if (resultList.isPresent()) {
            String formatted = "%s, %s".formatted(dialog.firstMember(), dialog.secondMember());
            throw new AlreadyExistingRecordException("Dialog", "members", formatted);
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(dialog);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public Optional<DialogEntity> findById(UUID id) throws DatabaseException {
        try {
            return Optional.ofNullable(em.find(DialogEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void remove(DialogEntity dialog) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(dialog);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public Optional<DialogEntity> findByMembers(MemberEntity firstMember, MemberEntity secondMember)
            throws DatabaseException {
        String q = """
            SELECT d FROM DialogEntity d
            WHERE
                (d.firstMember = :firstMember AND d.secondMember = :secondMember)
                OR (d.firstMember = :secondMember AND d.secondMember = :firstMember)
        """;
        TypedQuery<DialogEntity> query = em.createQuery(q, DialogEntity.class)
                .setParameter("firstMember", firstMember)
                .setParameter("secondMember", secondMember)
                .setMaxResults(1);
        try {
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
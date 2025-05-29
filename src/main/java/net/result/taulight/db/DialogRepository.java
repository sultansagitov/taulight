package net.result.taulight.db;

import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.exception.AlreadyExistingRecordException;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.Optional;
import java.util.UUID;

public class DialogRepository {
    private final JPAUtil jpaUtil;

    public DialogRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private DialogEntity save(@NotNull DialogEntity dialog) throws AlreadyExistingRecordException, DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
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
            DialogEntity managed = em.merge(dialog);

            dialog.firstMember().dialogsAsFirst().add(managed);
            dialog.secondMember().dialogsAsSecond().add(managed);

            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public DialogEntity create(TauMemberEntity firstMember, TauMemberEntity secondMember)
            throws AlreadyExistingRecordException, DatabaseException {
        return save(new DialogEntity(firstMember, secondMember));
    }

    public Optional<DialogEntity> findById(UUID id) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(DialogEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void remove(DialogEntity dialog) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
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

    public Optional<DialogEntity> findByMembers(TauMemberEntity firstMember, TauMemberEntity secondMember)
            throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        String q = """
            FROM DialogEntity
            WHERE
                (firstMember = :firstMember AND secondMember = :secondMember)
                OR (firstMember = :secondMember AND secondMember = :firstMember)
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
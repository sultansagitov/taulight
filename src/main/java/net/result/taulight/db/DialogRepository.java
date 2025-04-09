package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.exception.AlreadyExistingRecordException;
import org.jetbrains.annotations.NotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Optional;
import java.util.UUID;

public class DialogRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public DialogEntity save(DialogEntity dialog) throws AlreadyExistingRecordException, DatabaseException {
        Optional<DialogEntity> resultList = findByMembers(dialog.firstMember(), dialog.secondMember());

        if (resultList.isPresent()) {
            String formatted = "%s, %s".formatted(dialog.firstMember(), dialog.secondMember());
            throw new AlreadyExistingRecordException("Dialog", "members", formatted);
        }

        try {
            entityManager.persist(dialog);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
        return dialog;
    }

    public Optional<DialogEntity> findById(UUID id) throws DatabaseException {
        try {
            return Optional.ofNullable(entityManager.find(DialogEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void remove(DialogEntity dialog) throws DatabaseException {
        try {
            entityManager.remove(dialog);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public Optional<DialogEntity> findByMembers(MemberEntity firstMember, MemberEntity secondMember) {
        String q = """
            SELECT d FROM DialogEntity d
            WHERE
                (d.firstMember = :firstMember AND d.secondMember = :secondMember)
                OR (d.firstMember = :secondMember AND d.secondMember = :firstMember)
        """;
        TypedQuery<DialogEntity> query = entityManager.createQuery(q, DialogEntity.class)
                .setParameter("firstMember", firstMember)
                .setParameter("secondMember", secondMember)
                .setMaxResults(1);

        return query.getResultList().stream().findFirst();
    }

    public void addMemberToDialog(DialogEntity chat, MemberEntity member) throws DatabaseException {

    }

    public void removeMemberFromDialog(DialogEntity dialog, MemberEntity member) throws DatabaseException {

    }
}
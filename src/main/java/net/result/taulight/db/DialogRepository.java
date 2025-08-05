package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.result.sandnode.exception.AlreadyExistingRecordException;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

import java.util.Optional;

public class DialogRepository {
    private final JPAUtil jpaUtil;

    public DialogRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public DialogEntity create(TauMemberEntity firstMember, TauMemberEntity secondMember)
            throws AlreadyExistingRecordException, DatabaseException {

        Optional<DialogEntity> resultList = findByMembers(firstMember, secondMember);

        if (resultList.isPresent()) {
            String formatted = "%s, %s".formatted(firstMember, secondMember);
            throw new AlreadyExistingRecordException("Dialog", "members", formatted);
        }

        DialogEntity dialog = new DialogEntity(firstMember, secondMember);
        firstMember.dialogsAsFirst().add(dialog);
        secondMember.dialogsAsSecond().add(dialog);

        return jpaUtil.create(dialog);
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
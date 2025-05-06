package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import org.jetbrains.annotations.NotNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class LoginRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    private LoginEntity save(@NotNull LoginEntity login) throws DatabaseException {
        while (em.find(MemberEntity.class, login.id()) != null) {
            login.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            LoginEntity merged = em.merge(login);
            transaction.commit();
            return merged;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to save login entity", e);
        }
    }

    public LoginEntity create(MemberEntity member, boolean byPassword) throws DatabaseException {
        return save(new LoginEntity(member, byPassword));
    }
}

package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class LoginRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public LoginRepository(Container container) {}

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

    public LoginEntity create(MemberEntity member, String ip, boolean byPassword) throws DatabaseException {
        return save(new LoginEntity(member, ip, byPassword));
    }

    public List<LoginEntity> byPassword(@Nullable MemberEntity member) throws DatabaseException {
        try {
            String q = "FROM LoginEntity l WHERE l.member = :member AND l.byPassword = true";
            return em
                    .createQuery(q, LoginEntity.class)
                    .setParameter("member", member)
                    .getResultList();
        } catch (Exception e) {
            throw new DatabaseException("Failed to find login by member", e);
        }
    }
}

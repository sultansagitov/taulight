package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class LoginRepository {
    private final JPAUtil jpaUtil;

    public LoginRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private LoginEntity save(@NotNull LoginEntity login) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
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
        EntityManager em = jpaUtil.getEntityManager();
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

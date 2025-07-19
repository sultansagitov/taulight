package net.result.sandnode.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
            throw new DatabaseException(e);
        }
    }

    public LoginEntity create(MemberEntity member, KeyStorageEntity encryptor, String ip, String device)
            throws DatabaseException {
        return save(new LoginEntity(member, encryptor, ip, device));
    }

    public LoginEntity create(LoginEntity login, String ip) throws DatabaseException {
        return save(new LoginEntity(login, ip));
    }

    public Optional<LoginEntity> find(UUID uuid) {
        EntityManager em = jpaUtil.getEntityManager();
        LoginEntity login = em.find(LoginEntity.class, uuid);
        return Optional.ofNullable(login);
    }

    public List<LoginEntity> byDevice(@Nullable MemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "FROM LoginEntity l WHERE l.member = :member AND l.login IS NULL";
            return em
                    .createQuery(q, LoginEntity.class)
                    .setParameter("member", member)
                    .getResultList();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}

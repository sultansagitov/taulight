package net.result.sandnode.db;

import jakarta.persistence.EntityManager;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LoginRepository {
    private final JPAUtil jpaUtil;

    public LoginRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public LoginEntity create(MemberEntity member, KeyStorageEntity encryptor, String ip, String device)
            throws DatabaseException {
        return jpaUtil.create(new LoginEntity(member, encryptor, ip, device));
    }

    public LoginEntity create(LoginEntity login, String ip) throws DatabaseException {
        return jpaUtil.create(new LoginEntity(login, ip));
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

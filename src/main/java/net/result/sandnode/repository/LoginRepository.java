package net.result.sandnode.repository;

import jakarta.persistence.EntityManager;
import net.result.sandnode.entity.KeyStorageEntity;
import net.result.sandnode.entity.LoginEntity;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LoginRepository {
    private final JPAUtil jpaUtil;

    public LoginRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public LoginEntity create(MemberEntity member, KeyStorageEntity encryptor, String ip, String device) {
        return jpaUtil.create(new LoginEntity(member, encryptor, ip, device));
    }

    public LoginEntity create(LoginEntity login, String ip) {
        return jpaUtil.create(new LoginEntity(login, ip));
    }

    public List<LoginEntity> byDevice(@Nullable MemberEntity member) {
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

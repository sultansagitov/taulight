package net.result.sandnode.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import net.result.sandnode.db.MemberCreationListener;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.entity.FileEntity;
import net.result.sandnode.entity.KeyStorageEntity;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;

import java.util.List;
import java.util.Optional;

public class MemberRepository {
    private final JPAUtil jpaUtil;
    private final List<MemberCreationListener> creationListeners;

    public MemberRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
        creationListeners = container.getAll(MemberCreationListener.class);
    }

    private void notifyListeners(MemberEntity member) throws DatabaseException {
        for (var listener : creationListeners) {
            listener.onMemberCreated(member);
        }
    }

    public MemberEntity create(String nickname, String hashedPassword)
            throws DatabaseException, BusyNicknameException {
        if (findByNickname(nickname).isPresent()) throw new BusyNicknameException();
        var member = new MemberEntity(nickname, hashedPassword);
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        MemberEntity managed;
        try {
            transaction.begin();
            managed = em.merge(member);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }

        notifyListeners(managed);

        return managed;
    }

    public MemberEntity create(String nickname, String hashedPassword, AsymmetricKeyStorage keyStorage)
            throws DatabaseException, BusyNicknameException {
        if (findByNickname(nickname).isPresent()) throw new BusyNicknameException();
        MemberEntity member = new MemberEntity(nickname, hashedPassword);
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        MemberEntity managed;
        try {
            transaction.begin();
            KeyStorageEntity e = new KeyStorageEntity(keyStorage.encryption(), keyStorage.encodedPublicKey());
            KeyStorageEntity key = em.merge(e);
            member.setPublicKey(key);
            managed = em.merge(member);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }

        notifyListeners(managed);

        return managed;
    }

    public Optional<MemberEntity> findByNickname(String nickname) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "FROM MemberEntity WHERE nickname = :nickname AND deleted = false";
            return em
                    .createQuery(q, MemberEntity.class)
                    .setParameter("nickname", nickname)
                    .setMaxResults(1)
                    .getResultList()
                    .stream().findFirst();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean delete(MemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            var m = jpaUtil.find(MemberEntity.class, member.id());
            if (m.isEmpty()) return false;

            MemberEntity t = m.get();

            if (t.deleted()) return false;

            transaction.begin();
            t.setDeleted(true);
            em.merge(t);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public boolean setAvatar(MemberEntity member, FileEntity avatar) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            if (jpaUtil.find(MemberEntity.class, member.id()).isPresent()) {
                transaction.begin();
                member.setAvatar(avatar);
                em.merge(member);
                transaction.commit();
                return true;
            }

            return false;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public Optional<KeyStorageEntity> findPersonalKeyByNickname(String nickname) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        String q = """
            SELECT m.publicKey
            FROM MemberEntity m
            WHERE m.nickname = :nickname AND m.deleted = false
        """;
        TypedQuery<KeyStorageEntity> query = em.createQuery(q, KeyStorageEntity.class)
                .setParameter("nickname", nickname)
                .setMaxResults(1);
        try {
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}

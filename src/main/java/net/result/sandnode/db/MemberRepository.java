package net.result.sandnode.db;

import jakarta.persistence.TypedQuery;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.db.TauMemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Optional;

public class MemberRepository {
    private final JPAUtil jpaUtil;
    private final TauMemberRepository tauMemberRepo;

    public MemberRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
        tauMemberRepo = container.get(TauMemberRepository.class);
    }

    private MemberEntity save(MemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        while (em.find(MemberEntity.class, member.id()) != null) {
            member.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            MemberEntity merge = em.merge(member);
            transaction.commit();

            tauMemberRepo.create(merge);

            return merge;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    private MemberEntity save(MemberEntity member, AsymmetricKeyStorage keyStorage) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        while (em.find(MemberEntity.class, member.id()) != null) {
            member.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            KeyStorageEntity keyStorageEntity =
                    em.merge(new KeyStorageEntity(keyStorage.encryption(), keyStorage.encodedPublicKey()));
            member.setPublicKey(keyStorageEntity);

            MemberEntity managed = em.merge(member);

            transaction.commit();

            tauMemberRepo.create(managed);

            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public MemberEntity create(String nickname, String hashedPassword) throws DatabaseException, BusyNicknameException {
        if (findByNickname(nickname).isPresent()) throw new BusyNicknameException();
        return save(new MemberEntity(nickname, hashedPassword));
    }

    public MemberEntity create(String nickname, String hashedPassword, AsymmetricKeyStorage keyStorage)
            throws DatabaseException, BusyNicknameException {
        if (findByNickname(nickname).isPresent()) throw new BusyNicknameException();

        return save(new MemberEntity(nickname, hashedPassword), keyStorage);
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
            MemberEntity m = em.find(MemberEntity.class, member.id());
            if (m == null || m.deleted()) return false;

            transaction.begin();
            m.setDeleted(true);
            em.merge(m);
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
            if (em.find(MemberEntity.class, member.id()) != null) {
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

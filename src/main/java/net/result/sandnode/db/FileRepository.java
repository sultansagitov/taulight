package net.result.sandnode.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

import java.util.Optional;
import java.util.UUID;

public class FileRepository {
    private final JPAUtil jpaUtil;

    public FileRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private FileEntity save(FileEntity file) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        while (em.find(FileEntity.class, file.id()) != null) {
            file.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            FileEntity managed = em.merge(file);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public FileEntity create(String contentType, String filename) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        FileEntity managed = save(new FileEntity(contentType, filename));
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(managed);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }

        return managed;
    }

    public Optional<FileEntity> find(UUID id) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(FileEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}

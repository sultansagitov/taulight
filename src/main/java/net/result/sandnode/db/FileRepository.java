package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class FileRepository {
    private final EntityManager em;

    public FileRepository(Container container) {
        em = container.get(JPAUtil.class).getEntityManager();
    }

    private FileEntity save(FileEntity file) throws DatabaseException {
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
}

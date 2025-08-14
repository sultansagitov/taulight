package net.result.sandnode.db;

import jakarta.persistence.EntityManager;
import net.result.sandnode.entity.BaseEntity;
import net.result.sandnode.exception.DatabaseException;

import java.util.Optional;
import java.util.UUID;

public interface JPAUtil {
    EntityManager getEntityManager();

    void shutdown();

    <T extends BaseEntity> T refresh(T entity) throws DatabaseException;

    <T extends BaseEntity> T create(T entity) throws DatabaseException;

    <T extends BaseEntity> Optional<T> find(Class<T> clazz, UUID id) throws DatabaseException;
}

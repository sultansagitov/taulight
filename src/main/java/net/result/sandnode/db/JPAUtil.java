package net.result.sandnode.db;

import jakarta.persistence.EntityManager;
import net.result.sandnode.entity.BaseEntity;

import java.util.Optional;
import java.util.UUID;

public interface JPAUtil {
    EntityManager getEntityManager();

    void shutdown();

    <T extends BaseEntity> T refresh(T entity);

    <T extends BaseEntity> T create(T entity);

    <T extends BaseEntity> Optional<T> find(Class<T> clazz, UUID id);
}

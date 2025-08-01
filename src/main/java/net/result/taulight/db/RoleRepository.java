package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class RoleRepository {
    private final JPAUtil jpaUtil;

    public RoleRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private RoleEntity save(@NotNull RoleEntity reactionType) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        while (em.find(RoleEntity.class, reactionType.id()) != null) {
            reactionType.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            RoleEntity managed = em.merge(reactionType);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public RoleEntity create(GroupEntity group, String role) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        RoleEntity managed = save(new RoleEntity(group, role));

        group.roles().add(managed);
        em.merge(group);

        return managed;
    }

    public boolean addMember(RoleEntity role, TauMemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            Set<TauMemberEntity> members = new HashSet<>(role.members());
            if (members.contains(member)) return false;
            members.add(member);
            role.setMembers(members);

            Set<RoleEntity> roles = new HashSet<>(member.roles());
            roles.add(role);
            member.setRoles(roles);

            em.merge(role);
            em.merge(member);
            transaction.commit();

            return true;
        } catch (Exception e) {
            transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public boolean grantPermission(@NotNull RoleEntity role, @NotNull Permission permission) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            if (role.permissions().contains(permission)) return false;

            role.permissions().add(permission);

            transaction.begin();
            em.merge(role);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public boolean revokePermission(@NotNull RoleEntity role, @NotNull Permission permission) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            if (!role.permissions().contains(permission)) return false;

            role.permissions().remove(permission);

            transaction.begin();
            em.merge(role);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public Optional<RoleEntity> findById(UUID id) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(RoleEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}

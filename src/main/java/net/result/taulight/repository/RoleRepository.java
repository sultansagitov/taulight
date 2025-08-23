package net.result.taulight.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import net.result.taulight.db.Permission;
import net.result.taulight.entity.GroupEntity;
import net.result.taulight.entity.RoleEntity;
import net.result.taulight.entity.TauMemberEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoleRepository {
    private final JPAUtil jpaUtil;

    public RoleRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public RoleEntity create(GroupEntity group, String role) {
        EntityManager em = jpaUtil.getEntityManager();
        RoleEntity managed = jpaUtil.create(new RoleEntity(group, role));

        group.getRoles().add(managed);
        em.merge(group);

        return managed;
    }

    public boolean addMember(RoleEntity role, TauMemberEntity member) {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            Set<TauMemberEntity> members = new HashSet<>(role.members());
            if (members.contains(member)) return false;
            members.add(member);
            role.setMembers(members);

            Set<RoleEntity> roles = new HashSet<>(member.getRoles());
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

    public boolean grantPermission(@NotNull RoleEntity role, @NotNull Permission permission) {
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

    public boolean revokePermission(@NotNull RoleEntity role, @NotNull Permission permission) {
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

    public Set<Permission> getMemberPermissionsInGroup(GroupEntity group, TauMemberEntity member) {
        return Stream.concat(
                group.getRoles().stream()
                    .flatMap(role -> role.members().stream()
                        .filter(member::equals)
                        .flatMap(e -> role.permissions().stream())),
                group.getPermissions().stream()
            )
            .collect(Collectors.toSet());
    }

}

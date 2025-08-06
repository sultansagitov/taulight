package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.db.FileEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

import java.util.HashSet;
import java.util.Set;

public class GroupRepository {
    private final JPAUtil jpaUtil;

    public GroupRepository(Container container) {
        this.jpaUtil = container.get(JPAUtil.class);
    }

    public GroupEntity create(String title, TauMemberEntity owner) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            GroupEntity group = new GroupEntity(title, owner);
            group.setMembers(new HashSet<>(Set.of(owner)));

            GroupEntity managed = em.merge(group);

            transaction.commit();

            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public void delete(GroupEntity group) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            GroupEntity managed = em.merge(group);
            em.remove(managed);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public boolean addMember(GroupEntity group, TauMemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            GroupEntity managedGroup = em.merge(group);
            TauMemberEntity managedMember = em.merge(member);

            Set<TauMemberEntity> members = managedGroup.members();
            if (members == null) {
                members = new HashSet<>();
                managedGroup.setMembers(members);
            }

            if (!members.add(managedMember)) {
                transaction.rollback();
                return false;
            }

            managedMember.groups().add(managedGroup);

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public boolean removeMember(GroupEntity group, TauMemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            GroupEntity managedGroup = em.merge(group);
            TauMemberEntity managedMember = em.merge(member);

            Set<TauMemberEntity> members = managedGroup.members();
            if (members == null || !members.remove(managedMember)) {
                transaction.rollback();
                return false;
            }

            managedMember.groups().remove(managedGroup);

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public void setAvatar(GroupEntity group, FileEntity avatar) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            GroupEntity managed = em.merge(group);
            managed.setAvatar(avatar);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public boolean grantPermission(GroupEntity group, Permission permission) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            GroupEntity managed = em.merge(group);

            if (!managed.permissions().add(permission)) {
                transaction.rollback();
                return false;
            }

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public boolean revokePermission(GroupEntity group, Permission permission) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            GroupEntity managed = em.merge(group);

            if (!managed.permissions().remove(permission)) {
                transaction.rollback();
                return false;
            }

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public boolean contains(GroupEntity group, TauMemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "SELECT COUNT(g) FROM GroupEntity g JOIN g.members m WHERE g = :group AND m = :member";
            Long count = em.createQuery(q, Long.class)
                    .setParameter("group", group)
                    .setParameter("member", member)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}

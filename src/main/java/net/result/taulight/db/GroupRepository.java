package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.db.FileEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GroupRepository {
    private final JPAUtil jpaUtil;

    public GroupRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private GroupEntity save(GroupEntity group) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        while (em.find(GroupEntity.class, group.id()) != null) {
            group.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            GroupEntity managed = em.merge(group);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public GroupEntity create(String title, TauMemberEntity owner) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        GroupEntity managed = save(new GroupEntity(title, owner));
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            managed.setMembers(new HashSet<>(Set.of(owner)));
            em.merge(managed);

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }

        return managed;
    }

    public void delete(GroupEntity group) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(group);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public Optional<GroupEntity> findById(UUID id) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(GroupEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean addMember(GroupEntity group, TauMemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            Set<TauMemberEntity> members = group.members();
            if (members == null) {
                group.setMembers(Set.of(member));
            } else if (members.contains(member)) {
                return false;
            } else {
                group.members().add(member);
            }

            member.groups().add(group);

            transaction.begin();
            em.merge(group);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
        return true;
    }

    public boolean removeMember(GroupEntity group, TauMemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            Set<TauMemberEntity> members = group.members();

            if (members == null) {
                group.setMembers(Set.of());
            } else if (!members.contains(member)) {
                return false;
            } else {
                group.members().remove(member);
            }

            member.groups().remove(group);

            transaction.begin();
            em.merge(group);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
        return true;
    }

    public void setAvatar(GroupEntity group, FileEntity avatar) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            group.setAvatar(avatar);
            em.merge(group);
            transaction.commit();
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
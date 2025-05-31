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

public class ChannelRepository {
    private final JPAUtil jpaUtil;

    public ChannelRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private ChannelEntity save(ChannelEntity channel) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        while (em.find(ChannelEntity.class, channel.id()) != null) {
            channel.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            ChannelEntity managed = em.merge(channel);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public ChannelEntity create(String title, TauMemberEntity owner) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        ChannelEntity managed = save(new ChannelEntity(title, owner));
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

    public void delete(ChannelEntity channel) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(channel);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public Optional<ChannelEntity> findById(UUID id) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(ChannelEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean addMember(ChannelEntity channel, TauMemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            Set<TauMemberEntity> members = channel.members();
            if (members == null) {
                channel.setMembers(Set.of(member));
            } else if (members.contains(member)) {
                return false;
            } else {
                channel.members().add(member);
            }

            member.channels().add(channel);

            transaction.begin();
            em.merge(channel);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
        return true;
    }

    public boolean removeMember(ChannelEntity channel, TauMemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            Set<TauMemberEntity> members = channel.members();

            if (members == null) {
                channel.setMembers(Set.of());
            } else if (!members.contains(member)) {
                return false;
            } else {
                channel.members().remove(member);
            }

            member.channels().remove(channel);

            transaction.begin();
            em.merge(channel);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
        return true;
    }

    public void setAvatar(ChannelEntity channel, FileEntity avatar) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            channel.setAvatar(avatar);
            em.merge(channel);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public boolean contains(ChannelEntity channel, TauMemberEntity member) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "SELECT COUNT(c) FROM ChannelEntity c JOIN c.members m WHERE c = :channel AND m = :member";
            Long count = em.createQuery(q, Long.class)
                    .setParameter("channel", channel)
                    .setParameter("member", member)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
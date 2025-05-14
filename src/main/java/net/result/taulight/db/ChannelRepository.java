package net.result.taulight.db;

import net.result.sandnode.db.FileEntity;
import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;

public class ChannelRepository {
    private final EntityManager em;

    public ChannelRepository(Container container) {
        em = container.get(JPAUtil.class).getEntityManager();
    }

    private ChannelEntity save(ChannelEntity channel) throws DatabaseException {
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
        ChannelEntity managed = save(new ChannelEntity(title, owner));
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            managed.setMembers(new HashSet<>(Set.of(owner)));

            owner.channels().add(managed);
            em.merge(owner);

            em.merge(managed);

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }

        return managed;
    }

public void delete(ChannelEntity channel) throws DatabaseException {
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
        try {
            return Optional.ofNullable(em.find(ChannelEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean addMember(ChannelEntity channel, TauMemberEntity member) throws DatabaseException {
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
}
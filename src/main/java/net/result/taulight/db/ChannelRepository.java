package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;

public class ChannelRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public ChannelEntity save(ChannelEntity channel) throws DatabaseException {
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

    public ChannelEntity create(String title, MemberEntity owner) throws DatabaseException {
        ChannelEntity channel = new ChannelEntity(title, owner);
        return save(channel);
    }

    public Optional<ChannelEntity> findById(UUID id) throws DatabaseException {
        try {
            return Optional.ofNullable(em.find(ChannelEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void remove(ChannelEntity channel) throws DatabaseException {
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

    public boolean addMemberToChannel(ChannelEntity channel, MemberEntity member) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            Set<MemberEntity> members = channel.members();
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

    public boolean removeMemberFromChannel(ChannelEntity channel, MemberEntity member) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            Set<MemberEntity> members = channel.members();

            if (members == null) {
                channel.setMembers(Set.of(member));
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
}
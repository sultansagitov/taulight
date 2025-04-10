package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;

public class ChannelRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public void save(ChannelEntity channel) throws DatabaseException {
        while (em.contains(channel)) {
            channel.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(channel);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
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

    public void remove(ChannelEntity channel) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(channel);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public boolean addMemberToChannel(ChannelEntity channel, MemberEntity member) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            Set<MemberEntity> members = channel.members();
            if (members == null) {
                channel.setMembers(Set.of(member));
            } else if (members.stream().anyMatch(m -> m.id().equals(member.id()))) {
                return false;
            } else {
                members.add(member);
                channel.setMembers(members);
            }
            transaction.begin();
            em.merge(channel);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
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
            } else if (members.stream().noneMatch(m -> m.id().equals(member.id()))) {
                return false;
            } else {
                members.remove(member);
                channel.setMembers(members);
            }

            transaction.begin();
            em.merge(channel);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new DatabaseException(e);
        }
        return true;
    }

}
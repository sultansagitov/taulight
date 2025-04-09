package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.exception.AlreadyExistingRecordException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class ChannelRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public ChannelEntity save(ChannelEntity channel) throws AlreadyExistingRecordException, DatabaseException {
        try {
            if (channel.id() == null) {
                throw new IllegalArgumentException("Channel ID cannot be null.");
            }

            if (findById(channel.id()).isPresent()) {
                throw new AlreadyExistingRecordException("Channel", "ID", channel.id());
            }

            entityManager.persist(channel);
            return channel;
        } catch (AlreadyExistingRecordException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public Optional<ChannelEntity> findById(UUID id) throws DatabaseException {
        try {
            return Optional.ofNullable(entityManager.find(ChannelEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void remove(ChannelEntity channel) throws DatabaseException {
        try {
            entityManager.remove(channel);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public Collection<MemberEntity> findMembersByChannel(ChannelEntity channel) throws DatabaseException {

    }

    public void addMemberToChannel(ChannelEntity chat, MemberEntity member) throws DatabaseException {

    }

    public void removeMemberFromChannel(ChannelEntity channel, MemberEntity member) throws DatabaseException {

    }
}
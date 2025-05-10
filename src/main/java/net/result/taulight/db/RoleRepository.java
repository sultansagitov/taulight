package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import org.jetbrains.annotations.NotNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.HashSet;
import java.util.Set;

public class RoleRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public RoleRepository(Container container) {}

    private RoleEntity save(@NotNull RoleEntity reactionType) throws DatabaseException {
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
            throw new DatabaseException("Failed to save role", e);
        }
    }

    public RoleEntity create(ChannelEntity channel, String role) throws DatabaseException {
        RoleEntity managed = save(new RoleEntity(channel, role));

        channel.roles().add(managed);
        em.merge(channel);

        return managed;
    }

    public boolean addMember(RoleEntity role, TauMemberEntity member) throws DatabaseException {
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
            throw new DatabaseException("Failed to add member to role", e);
        }
    }
}

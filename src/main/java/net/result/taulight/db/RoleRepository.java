package net.result.taulight.db;

import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.HashSet;
import java.util.Set;

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
            throw new DatabaseException("Failed to save role", e);
        }
    }

    public RoleEntity create(ChannelEntity channel, String role) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        RoleEntity managed = save(new RoleEntity(channel, role));

        channel.roles().add(managed);
        em.merge(channel);

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
            throw new DatabaseException("Failed to add member to role", e);
        }
    }
}

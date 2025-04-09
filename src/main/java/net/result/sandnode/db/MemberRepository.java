package net.result.sandnode.db;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class MemberRepository {
    private final EntityManager em;

    public MemberRepository(EntityManager em) {
        this.em = em;
    }

    public void save(MemberEntity member) {
        em.getTransaction().begin();
        em.persist(member);
        em.getTransaction().commit();
    }

    public Optional<MemberEntity> findByNickname(String nickname) {
        List<MemberEntity> results = em.createQuery(
                "FROM MemberEntity WHERE nickname = :nickname", MemberEntity.class)
                .setParameter("nickname", nickname)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}

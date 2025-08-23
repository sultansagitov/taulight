package net.result.taulight.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.result.sandnode.entity.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@SuppressWarnings("unused")
@Entity
public class ReactionTypeEntity extends BaseEntity {
    private String name;

    @ManyToOne
    private ReactionPackageEntity reactionPackage;

    @OneToMany
    private Set<ReactionEntryEntity> reactionEntries = new HashSet<>();

    public ReactionTypeEntity(String name, ReactionPackageEntity reactionPackage) {
        setName(name);
        setReactionPackage(reactionPackage);
    }
}
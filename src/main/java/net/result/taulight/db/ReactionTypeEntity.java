package net.result.taulight.db;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import net.result.sandnode.db.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
public class ReactionTypeEntity extends BaseEntity {
    private String name;

    @ManyToOne
    private ReactionPackageEntity reactionPackage;


    @OneToMany
    private Set<ReactionEntryEntity> reactionEntries = new HashSet<>();

    public ReactionTypeEntity() {}

    public ReactionTypeEntity(String name, ReactionPackageEntity reactionPackage) {
        setName(name);
        setReactionPackage(reactionPackage);
    }

    public String name() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public ReactionPackageEntity reactionPackage() {
        return reactionPackage;
    }

    public void setReactionPackage(ReactionPackageEntity reactionPackage) {
        this.reactionPackage = reactionPackage;
    }

    public Set<ReactionEntryEntity> reactionEntries() {
        return reactionEntries;
    }

    public void setReactionEntries(Set<ReactionEntryEntity> reactionEntries) {
        this.reactionEntries = reactionEntries;
    }
}
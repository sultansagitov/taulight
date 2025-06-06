package net.result.taulight.db;

import net.result.sandnode.db.SandnodeEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
public class ReactionTypeEntity extends SandnodeEntity {
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
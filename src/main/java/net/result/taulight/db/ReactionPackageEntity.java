package net.result.taulight.db;

import net.result.sandnode.db.SandnodeEntity;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
public class ReactionPackageEntity extends SandnodeEntity {
    private String name;

    @OneToMany(mappedBy = "reactionPackage")
    private Set<ReactionTypeEntity> reactionTypes = new HashSet<>();

    public ReactionPackageEntity() {}

    public ReactionPackageEntity(String name) {
        setName(name);
    }

    public String name() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public Set<ReactionTypeEntity> reactionTypes() {
        return reactionTypes;
    }

    public void setReactionTypes(Set<ReactionTypeEntity> reactionTypes) {
        this.reactionTypes = reactionTypes;
    }
}

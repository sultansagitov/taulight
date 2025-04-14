package net.result.taulight.db;

import net.result.sandnode.db.SandnodeEntity;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
public class ReactionTypeEntity extends SandnodeEntity {
    private String name;
    private String packageName;

    @OneToMany
    private Set<ReactionEntryEntity> reactionEntries = new HashSet<>();

    public ReactionTypeEntity() {}

    public ReactionTypeEntity(String name, String packageName) {
        setName(name);
        setPackageName(packageName);
    }

    public String name() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String packageName() {
        return packageName;
    }

    private void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Set<ReactionEntryEntity> reactionEntries() {
        return reactionEntries;
    }

    public void setReactionEntries(Set<ReactionEntryEntity> reactionEntries) {
        this.reactionEntries = reactionEntries;
    }
}
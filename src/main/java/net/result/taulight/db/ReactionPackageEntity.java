package net.result.taulight.db;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import net.result.sandnode.db.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
public class ReactionPackageEntity extends BaseEntity {
    private String name;
    private String description;

    @ManyToOne
    private TauMemberEntity author;

    @OneToMany(mappedBy = "reactionPackage")
    private Set<ReactionTypeEntity> reactionTypes = new HashSet<>();

    public ReactionPackageEntity() {}

    public ReactionPackageEntity(String name, String description) {
        setName(name);
        setDescription(description);
    }

    public String name() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TauMemberEntity author() {
        return author;
    }

    public void setAuthor(TauMemberEntity author) {
        this.author = author;
    }

    public Set<ReactionTypeEntity> reactionTypes() {
        return reactionTypes;
    }

    public void setReactionTypes(Set<ReactionTypeEntity> reactionTypes) {
        this.reactionTypes = reactionTypes;
    }
}

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

@SuppressWarnings("unused")
@Getter
@Setter
@NoArgsConstructor
@Entity
public class ReactionPackageEntity extends BaseEntity {
    private String name;
    private String description;

    @ManyToOne
    private TauMemberEntity author;

    @OneToMany(mappedBy = "reactionPackage")
    private Set<ReactionTypeEntity> reactionTypes = new HashSet<>();

    public ReactionPackageEntity(String name, String description) {
        setName(name);
        setDescription(description);
    }
}

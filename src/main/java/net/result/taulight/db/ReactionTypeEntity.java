package net.result.taulight.db;

import net.result.sandnode.db.SandnodeEntity;

import javax.persistence.Entity;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
public class ReactionTypeEntity extends SandnodeEntity {
    private String name;
    private String packageName;

    @SuppressWarnings("unused")
    public ReactionTypeEntity() {
        super();
    }

    public ReactionTypeEntity(UUID id, ZonedDateTime creationDate, String name, String packageName) {
        super(id, creationDate);
        setName(name);
        setPackageName(packageName);
    }

    public ReactionTypeEntity(String name, String packageName) {
        setName(name);
        setPackageName(packageName);
    }

    public String name() {
        return name;
    }

    public String packageName() {
        return packageName;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
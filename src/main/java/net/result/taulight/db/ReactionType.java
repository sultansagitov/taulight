package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.UUID;

public class ReactionType extends TaulightObject {
    @JsonProperty("name")
    private String name;
    @JsonProperty("package_name")
    private String packageName;

    public ReactionType() {
        super();
    }

    public ReactionType(TauDatabase database, UUID id, ZonedDateTime createdAt, String name, String packageName) {
        super(database, id, createdAt);
        this.name = name;
        this.packageName = packageName;
    }

    public String name() {
        return name;
    }

    public String packageName() {
        return packageName;
    }
}
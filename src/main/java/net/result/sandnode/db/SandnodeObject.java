package net.result.sandnode.db;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public abstract class SandnodeObject {
    @JsonProperty
    private UUID id = null;

    public UUID id() {
        return id;
    }

    public void setID(UUID id) {
        this.id = id;
    }

    public void setRandomID() {
        this.id = UUID.randomUUID();
    }
}

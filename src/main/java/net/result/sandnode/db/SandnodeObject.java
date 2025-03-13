package net.result.sandnode.db;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.UUID;

public abstract class SandnodeObject {
    @JsonProperty
    private UUID id = null;

    @JsonProperty("creation-date")
    private ZonedDateTime creationDate;

    public UUID id() {
        return id;
    }

    public void setID(UUID id) {
        this.id = id;
    }

    public void setRandomID() {
        this.id = UUID.randomUUID();
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreationDateNow() {
        setCreationDate(ZonedDateTime.now());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof SandnodeObject sn && id().equals(sn.id());
    }

}

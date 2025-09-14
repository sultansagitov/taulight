package net.result.sandnode.key;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public abstract class Source {
    @JsonProperty
    public ZonedDateTime createdAt;

    public Source() {}

    protected Source(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

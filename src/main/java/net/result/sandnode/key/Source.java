package net.result.sandnode.key;

import java.time.ZonedDateTime;

public abstract class Source {
    public final ZonedDateTime createdAt;

    protected Source() {
        this(ZonedDateTime.now());
    }

    protected Source(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

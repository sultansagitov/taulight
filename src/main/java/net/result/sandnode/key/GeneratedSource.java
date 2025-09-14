package net.result.sandnode.key;

import java.time.ZonedDateTime;

public class GeneratedSource extends Source {
    @SuppressWarnings("unused")
    public GeneratedSource() {}

    public GeneratedSource(ZonedDateTime createdAt) {
        super(createdAt);
    }

    public static GeneratedSource now() {
        return new GeneratedSource(ZonedDateTime.now());
    }
}

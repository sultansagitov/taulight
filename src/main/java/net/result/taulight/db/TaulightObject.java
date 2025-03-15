package net.result.taulight.db;

import net.result.sandnode.db.SandnodeObject;

import java.time.ZonedDateTime;
import java.util.UUID;

public class TaulightObject extends SandnodeObject {
    public TaulightObject() {
        super();
    }

    public TaulightObject(TauDatabase database) {
        super(database);
    }

    public TaulightObject(UUID id, ZonedDateTime createdAt, TauDatabase database) {
        super(database, id, createdAt);
    }

    @Override
    public TauDatabase database() {
        return (TauDatabase) super.database();
    }
}

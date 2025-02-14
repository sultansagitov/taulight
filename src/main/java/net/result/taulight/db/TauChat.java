package net.result.taulight.db;

import java.util.UUID;

public abstract class TauChat {
    private final UUID id;

    public TauChat(UUID id) {
        this.id = id;
    }

    public UUID getID() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof TauChat chat && id.equals(chat.id);
    }
}

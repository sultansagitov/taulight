package net.result.taulight.db;

public abstract class TauChat {
    private final String id;

    public TauChat(String id) {
        this.id = id;
    }

    public String getID() {
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

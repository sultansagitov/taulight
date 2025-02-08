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
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof TauChat that)) return false;
        return id.equals(that.id);
    }
}

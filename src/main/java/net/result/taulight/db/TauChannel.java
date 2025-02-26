package net.result.taulight.db;

import net.result.sandnode.db.Member;

import java.util.UUID;

public class TauChannel extends TauChat {
    private final String title;
    private final Member owner;

    public TauChannel(UUID id, TauDatabase database, String title, Member owner) {
        super(id, database);
        this.title = title;
        this.owner = owner;
    }

    public TauChannel(TauDatabase database, String title, Member owner) {
        this(UUID.randomUUID(), database, title, owner);
    }

    public String title() {
        return title;
    }

    public Member owner() {
        return owner;
    }

}

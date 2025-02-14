package net.result.taulight.db;

import net.result.sandnode.db.Member;

import java.util.UUID;

public class TauChannel extends TauChat {
    private final String title;
    private final Member owner;

    public TauChannel(UUID id, String title, Member owner) {
        super(id);
        this.title = title;
        this.owner = owner;
    }

    public TauChannel(String title, Member owner) {
        this(UUID.randomUUID(), title, owner);
    }

    public String getTitle() {
        return title;
    }

    public Member getOwner() {
        return owner;
    }

}

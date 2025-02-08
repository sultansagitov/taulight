package net.result.taulight.db;

import net.result.sandnode.db.Member;

public class TauChannel extends TauChat {
    private final String title;
    private final Member owner;

    public TauChannel(String id, String title, Member owner) {
        super(id);
        this.title = title;
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public Member getOwner() {
        return owner;
    }

}

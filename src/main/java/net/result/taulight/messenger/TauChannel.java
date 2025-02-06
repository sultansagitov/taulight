package net.result.taulight.messenger;

import net.result.sandnode.db.Member;

public class TauChannel extends TauChat {
    private final String title;
    private final Member owner;

    public TauChannel(String title, String id, Member owner) {
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

package net.result.taulight.messenger;

import net.result.sandnode.db.Member;
import net.result.sandnode.group.GroupManager;

public class TauChannel extends TauChat {
    private final String title;
    private final Member owner;

    public TauChannel(String title, String id, Member owner, GroupManager manager) {
        super(id, manager);
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

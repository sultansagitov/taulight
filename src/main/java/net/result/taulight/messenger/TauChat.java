package net.result.taulight.messenger;

import net.result.sandnode.group.GroupManager;
import net.result.taulight.group.TauChatGroup;

public class TauChat {
    private final String id;
    public final TauChatGroup group;

    public TauChat(String id, GroupManager manager) {
        this.id = id;
        group = new TauChatGroup(id);
        manager.add(group);
    }

    public String getID() {
        return id;
    }
}

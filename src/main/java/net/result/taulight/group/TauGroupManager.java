package net.result.taulight.group;

import net.result.sandnode.group.GroupManager;
import net.result.taulight.db.ChatEntity;

public interface TauGroupManager extends GroupManager {
    ChatGroup getGroup(ChatEntity chat);
}

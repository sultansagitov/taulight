package net.result.taulight.group;

import net.result.sandnode.group.GroupManager;
import net.result.taulight.messenger.TauChat;

public interface TauGroupManager extends GroupManager {
    TauChatGroup getGroup(TauChat chat);
}

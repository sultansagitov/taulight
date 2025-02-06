package net.result.taulight.group;

import net.result.sandnode.group.HashSetGroupManager;
import net.result.taulight.messenger.TauChat;

import java.util.HashMap;
import java.util.Map;

public class HashSetTauGroupManager extends HashSetGroupManager implements TauGroupManager {
    Map<String, TauChatGroup> map = new HashMap<>();

    @Override
    public TauChatGroup getGroup(TauChat chat) {
        String id = chat.getID();

        if (!map.containsKey(id)) {
            TauChatGroup group = new TauChatGroup(id);
            add(group);
            map.put(id, group);
            return group;
        }

        return map.get(id);
    }
}
